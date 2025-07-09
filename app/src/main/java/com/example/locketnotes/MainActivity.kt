package com.example.locketnotes

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.locketnotes.ui.theme.LocketNotesTheme
import com.example.locketnotes.presentation.Camera.CameraScreen
import com.example.locketnotes.presentation.Camera.LoginScreen
import com.example.locketnotes.presentation.Camera.MyProfileScreen
import com.example.locketnotes.presentation.Camera.RegisterScreen
import com.example.locketnotes.presentation.Camera.SettingsScreen
import com.example.locketnotes.presentation.chat.MessengerScreen
import com.example.locketnotes.presentation.friends.FriendsScreen
//import com.example.locketnotes.presentation.friends.FriendsScreen
import com.example.locketnotes.presentation.screens.MyStoriesScreen
import com.example.locketnotes.presentation.screens.NewsFeedScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!arePermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, CAMERA_PERMISSION, 100
            )
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
        val CAMERA_PERMISSION = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}
@Composable
fun AppNavigation(activity: Activity) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("register") { RegisterScreen(navController = navController) }
        composable("login") { LoginScreen(navController) }
        composable("camera") {CameraScreen(activity, navController) }
        composable("chat") {MessengerScreen()}
        composable("MyStories") { MyStoriesScreen(navController)}
        composable("setting") { SettingsScreen(navController) }
        composable("myprofile") { MyProfileScreen(navController) }
        composable("friends"){FriendsScreen(navController)}
        composable("newsfeed") { NewsFeedScreen(navController = navController) }
    }
}