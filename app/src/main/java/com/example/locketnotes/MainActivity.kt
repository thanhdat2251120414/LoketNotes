package com.example.locketnotes

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.locketnotes.presentation.Camera.*
import com.example.locketnotes.presentation.chat.MessengerScreen
import com.example.locketnotes.presentation.friends.FriendsScreen
import com.example.locketnotes.presentation.screens.MyStoriesScreen
import com.example.locketnotes.presentation.screens.NewsFeedScreen
import com.example.locketnotes.ui.theme.LocketNotesTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!arePermissionsGranted()) {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, 100)
        }

        setContent {
            LocketNotesTheme {
                AppNavigation(this)
            }
        }
    }

    fun arePermissionsGranted(): Boolean {
        return CAMERA_PERMISSION.all { permission ->
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    companion object {
        private val CAMERA_PERMISSION = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}
@Composable
fun AppNavigation(activity: Activity) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Auto-login: kiểm tra nếu đã đăng nhập -> vào camera, chưa thì login
    val startDestination = if (currentUser == null) "login" else "camera"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("register") { RegisterScreen(navController = navController) }
        composable("login") { LoginScreen(navController) }
        composable("camera") { CameraScreen(activity, navController) }
        composable("chat") { MessengerScreen() }
        composable("MyStories") { MyStoriesScreen(navController) }
        composable("setting") { SettingsScreen(navController) }
        composable("myprofile") { MyProfileScreen(navController) }
        composable("friends") { FriendsScreen(navController) }
        composable("newsfeed") { NewsFeedScreen(navController = navController) }
    }
}