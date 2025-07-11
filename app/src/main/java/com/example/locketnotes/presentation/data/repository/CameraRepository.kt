package com.example.locketnotes.presentation.data.repository

import android.graphics.Bitmap
import androidx.camera.view.LifecycleCameraController



interface CameraRepository {
    suspend fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap, String) -> Unit
    )

    suspend fun savePhotoToPermanentStorage(bitmap: Bitmap)

    suspend fun recordVideo(
        controller: LifecycleCameraController
    )

    suspend fun uploadPhotoToFirebase(
        bitmap: Bitmap,
        message: String,
        isPublic: Boolean,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    )
}