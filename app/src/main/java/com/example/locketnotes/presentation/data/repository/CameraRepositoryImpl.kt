package com.example.locketnotes.presentation.data.repository

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.video.Recording
import androidx.camera.view.LifecycleCameraController
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.video.AudioConfig
import androidx.core.content.ContextCompat
import com.example.homeloketnotes.data.repository.uploadToCloudinary
import com.example.locketnotes.R
import com.example.locketnotes.presentation.domain.model.PhotoUpload
import com.example.locketnotes.presentation.domain.repositry.CameraRepository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


class CameraRepositoryImpl @Inject constructor(
    private val application: Application
): CameraRepository {

    private var recording: Recording? = null

    override suspend fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap, String) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(application),
            object : ImageCapture.OnImageCapturedCallback() {
                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    try {
                        val matrix = Matrix().apply {
                            postRotate(image.imageInfo.rotationDegrees.toFloat())
                        }

                        val imageBitmap = Bitmap.createBitmap(
                            image.toBitmap(),
                            0, 0,
                            image.width, image.height,
                            matrix, true
                        )


                        val tempUri = createTempImageUri(imageBitmap)


                        onPhotoTaken(imageBitmap, tempUri)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun createTempImageUri(bitmap: Bitmap): String {
        return try {
            val tempFile =
                File(application.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            tempFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun savePhotoToPermanentStorage(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val resolver: ContentResolver = application.contentResolver

            val imageCollection = MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

            val appName = application.getString(R.string.app_name)
            val timeInMillis = System.currentTimeMillis()

            val imageContentValues: ContentValues = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "${timeInMillis}_image.jpg"
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/$appName"
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.DATE_TAKEN, timeInMillis)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val imageMediaStoreUri: Uri? = resolver.insert(
                imageCollection, imageContentValues
            )

            imageMediaStoreUri?.let { uri ->
                try {
                    resolver.openOutputStream(uri)?.let { outputStream ->
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG, 100, outputStream
                        )
                    }

                    imageContentValues.clear()
                    imageContentValues.put(
                        MediaStore.MediaColumns.IS_PENDING, 0
                    )
                    resolver.update(
                        uri, imageContentValues, null, null
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                    resolver.delete(uri, null, null)
                    throw e
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun recordVideo(
        controller: LifecycleCameraController
    ) {
        if (recording != null) {
            recording?.stop()
            recording = null
            return
        }

        val timeInMillis = System.currentTimeMillis()
        val file = File(
            application.filesDir,
            "${timeInMillis}_video" + ".mp4"
        )

        recording = controller.startRecording(
            FileOutputOptions.Builder(file).build(),
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(application)
        ) { event ->
            if (event is VideoRecordEvent.Finalize) {
                if (event.hasError()) {
                    recording?.close()
                    recording = null
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        saveVideo(file)
                    }
                }
            }
        }
    }





    private val database = Firebase.database
    private val auth = Firebase.auth

    override suspend fun uploadPhotoToFirebase(
        bitmap: Bitmap,
        message: String,
        isPublic: Boolean,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        uploadToCloudinary(bitmap, { uploadedUrl ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        onError(Exception("Không tìm thấy người dùng đã đăng nhập"))
                        return@launch
                    }

                    val photoKey = database.reference
                        .child("user")
                        .child(userId)
                        .child("photos")
                        .push()
                        .key

                    if (photoKey == null) {
                        onError(Exception("Không thể tạo key ảnh"))
                        return@launch
                    }

                    val photoUpload = PhotoUpload(
                        id = photoKey,
                        imageUrl = uploadedUrl,
                        message = message,
                        isPublic = isPublic,
                        timestamp = System.currentTimeMillis(),
                        userId = userId
                    )

                    database.reference
                        .child("user")
                        .child(userId)
                        .child("photos")
                        .child(photoKey)
                        .setValue(photoUpload)
                        .addOnSuccessListener {
                            onSuccess("Ảnh đã được lưu vào tài khoản người dùng!")
                        }
                        .addOnFailureListener { ex -> onError(ex) }

                } catch (e: Exception) {
                    onError(e)
                }
            }
        }, onError)
    }




    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveVideo(file: File) {
        withContext(Dispatchers.IO) {
            val resolver: ContentResolver = application.contentResolver

            val videoCollection = MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

            val appName = application.getString(R.string.app_name)
            val timeInMillis = System.currentTimeMillis()

            val videoContentValues: ContentValues = ContentValues().apply {
                put(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    file.name
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/$appName"
                )
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.DATE_ADDED, timeInMillis / 1000)
                put(MediaStore.MediaColumns.DATE_MODIFIED, timeInMillis / 1000)
                put(MediaStore.MediaColumns.DATE_TAKEN, timeInMillis)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            val videoMediaStoreUri: Uri? = resolver.insert(
                videoCollection, videoContentValues
            )

            videoMediaStoreUri?.let { uri ->
                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        resolver.openInputStream(
                            Uri.fromFile(file)
                        )?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    videoContentValues.clear()
                    videoContentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(
                        uri, videoContentValues, null, null
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                    resolver.delete(uri, null, null)
                }
            }
        }
    }

}

