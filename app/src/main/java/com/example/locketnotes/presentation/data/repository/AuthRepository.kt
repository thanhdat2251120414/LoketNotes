package com.example.locketnotes.presentation.data.repository

import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.navigation.NavController
import com.example.locketnotes.R
import com.example.locketnotes.presentation.domain.model.UserData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.common.api.CommonStatusCodes




fun registerUser(
    username: String,
    email: String,
    password: String,
    context: Context,
    navController: NavController,
    onError: (String) -> Unit
) {
    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
        onError("Vui lòng điền đầy đủ thông tin")
        return
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onError("Email không hợp lệ")
        return
    }

    if (password.length < 8) {
        onError("Mật khẩu phải từ 8 ký tự trở lên")
        return
    }

    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser ?: return@addOnCompleteListener
                val userId = user.uid

                val userData = UserData(
                    userId = userId,
                    username = username,
                    email = email
                )

                database.child("user").child(userId).setValue(userData)

                user.sendEmailVerification()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Đã gửi email xác thực. Vui lòng kiểm tra hộp thư.",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Không thể gửi email xác thực: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

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
    if (email.isEmpty() || password.isEmpty()) {
        onError("Vui lòng nhập đầy đủ thông tin")
        return
    }

    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.isEmailVerified) {
                    navController.navigate("camera") {
                        popUpTo(0)
                    }
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

// Hàm xử lý Google Sign-In result
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
                        // Lưu thông tin user vào database
                        saveGoogleUserToDatabase(user, context, navController)
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

// Hàm lưu thông tin Google user vào database
private fun saveGoogleUserToDatabase(
    user: FirebaseUser,
    context: Context,
    navController: NavController
) {
    val database = FirebaseDatabase.getInstance().reference
    val userId = user.uid

    val userData = UserData(
        userId = userId,
        username = user.displayName ?: "Google User",
        email = user.email ?: ""
    )

    database.child("user").child(userId).setValue(userData)
        .addOnSuccessListener {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            navController.navigate("camera") {
                popUpTo(0)
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(
                context,
                "Lỗi lưu thông tin: ${exception.message}",
                Toast.LENGTH_LONG
            ).show()
        }
}