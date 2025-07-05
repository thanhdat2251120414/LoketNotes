package com.example.homeloketnotes.data.repository

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.navigation.NavController

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import android.graphics.Bitmap
import com.example.locketnotes.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

fun registerUser(
    username: String,
    email: String,
    password: String,
    context: Context,
    navController: NavController,
    gender: String?,
    birthDate: Long?,
    profileImageUrl: String?,
    onError: (String) -> Unit
) {
    if (username.isBlank() || email.isBlank() || password.isBlank()) {
        onError("Vui lòng điền đầy đủ thông tin")
        return
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onError("Email không hợp lệ")
        return
    }

    if (password.length < 8) {
        onError("Mật khẩu phải từ 8 ký tự trở lên")
        return
    }

    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser ?: return@addOnCompleteListener
                val userId = user.uid

                saveUserToDatabase(
                    userId = userId,
                    username = username,
                    email = email,
                    gender = gender,
                    birthDate = birthDate,
                    profileImageUrl = profileImageUrl,
                    onSuccess = {
                        user.sendEmailVerification()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Đã gửi email xác thực. Vui lòng kiểm tra hộp thư.",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate("login") { popUpTo(0) }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Không thể gửi email xác thực: ${it.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    },
                    onError = { error ->
                        onError("Lỗi lưu thông tin: $error")
                    }
                )
            } else {
                onError("Lỗi đăng ký: ${task.exception?.message}")
            }
        }
}

fun loginUser(
    email: String,
    password: String,
    context: Context,
    navController: NavController,
    onError: (String) -> Unit
) {
    if (email.isBlank() || password.isBlank()) {
        onError("Vui lòng nhập đầy đủ thông tin")
        return
    }

    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.isEmailVerified) {
                    navController.navigate("camera") { popUpTo(0) }
                } else {
                    onError("Tài khoản chưa xác minh. Vui lòng kiểm tra email.")
                }
            } else {
                onError("Sai email hoặc mật khẩu: ${task.exception?.message}")
            }
        }
}

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val clientId = context.getString(R.string.default_web_client_id)
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, gso)
}

fun handleGoogleSignInResult(
    data: Intent?,
    context: Context,
    navController: NavController,
    onError: (String) -> Unit
) {
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    try {
        val account = task.getResult(ApiException::class.java)
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val user = authTask.result?.user
                    if (user != null) {
                        saveUserToDatabase(
                            userId = user.uid,
                            username = user.displayName ?: "Google User",
                            email = user.email ?: "",
                            gender = null,
                            birthDate = null,
                            profileImageUrl = user.photoUrl?.toString(),
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Đăng nhập thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("camera") { popUpTo(0) }
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    "Lỗi lưu thông tin: $error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                } else {
                    onError("Đăng nhập Firebase thất bại: ${authTask.exception?.message}")
                }
            }
    } catch (e: ApiException) {
        val errorMessage = when (e.statusCode) {
            CommonStatusCodes.CANCELED -> "Đăng nhập bị hủy"
            CommonStatusCodes.ERROR -> "Đăng nhập thất bại"
            CommonStatusCodes.NETWORK_ERROR -> "Lỗi mạng"
            else -> "Lỗi Google: ${e.message}"
        }
        onError(errorMessage)
    }
}

fun saveUserToDatabase(
    userId: String,
    username: String,
    email: String,
    gender: String?,
    birthDate: Long?,
    profileImageUrl: String?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance().reference
    val updates = mutableMapOf<String, Any?>()

    updates["username"] = username
    updates["email"] = email
    updates["gender"] = gender
    updates["birthDate"] = birthDate
    updates["profileImageUrl"] = profileImageUrl

    database.child("user").child(userId)
        .updateChildren(updates)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { ex -> onError(ex.message ?: "Không rõ lỗi") }
}


suspend fun uploadToCloudinary(
    bitmap: Bitmap,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val imageBytes = baos.toByteArray()
            val base64Image =
                android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)

            val cloudName = "dtzum2dzo"
            val apiKey = "373418943822732"
            val apiSecret = "sdLDlYBm7zau8htutbzlmQuzQOY"

            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val toSign = "timestamp=$timestamp$apiSecret"
            val md = MessageDigest.getInstance("SHA-1")
            val signature = md.digest(toSign.toByteArray()).joinToString("") { "%02x".format(it) }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "data:image/jpeg;base64,$base64Image")
                .addFormDataPart("timestamp", timestamp)
                .addFormDataPart("api_key", apiKey)
                .addFormDataPart("signature", signature)
                .build()

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                onError(Exception("Upload to Cloudinary failed: ${response.message}"))
                return@withContext
            }

            val json = JSONObject(response.body?.string() ?: "")
            val uploadedUrl = json.getString("secure_url")
            onSuccess(uploadedUrl)

        } catch (e: Exception) {
            onError(e)
        }
    }
}
