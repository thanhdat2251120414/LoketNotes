package com.example.locketnotes.presentation.data.repository

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.locketnotes.presentation.domain.model.UserData
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase


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

fun setupGoogleLogin(
    context: Context,
    onSuccess: (FirebaseUser?) -> Unit,
    onError: (String) -> Unit
): Pair<GoogleSignInClient, ManagedActivityResultLauncher<Intent, ActivityResult>> {

    // Get client ID from resources or BuildConfig instead of hardcoding
    val clientId = context.getString(R.string.default_web_client_id)

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .build()

    val googleClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            onSuccess(authTask.result?.user)
                        } else {
                            val errorMessage = authTask.exception?.message
                                ?: "Đăng nhập Firebase thất bại"
                            onError(errorMessage)
                        }
                    }
            } catch (e: ApiException) {
                val errorMessage = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Đăng nhập bị hủy"
                    GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Đăng nhập thất bại"
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Lỗi mạng"
                    else -> "Lỗi Google: ${e.message}"
                }
                onError(errorMessage)
            }
        } else {
            onError("Đăng nhập bị hủy")
        }
    }

    return Pair(googleClient, launcher)
}