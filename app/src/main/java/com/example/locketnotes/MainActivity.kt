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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.locketnotes.presentation.Camera.*
import com.example.locketnotes.presentation.chat.ChatScreen
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
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser == null) "login" else "camera"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("register") {
            RegisterScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("camera") {
            CameraScreen(activity, navController)

        }

        composable("messenger") {
            MessengerScreen(
                navController = navController,
                onChatClick = { partnerId ->
                    navController.navigate("chat/$partnerId")
                }
            )
        }



        composable(
            "chat/{partnerId}",
            arguments = listOf(navArgument("partnerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId") ?: ""
            ChatScreen(
                partnerId = partnerId,
                navController = navController // ✅ Thêm dòng này
            )
        }

        composable("MyStories") {
            MyStoriesScreen(navController)
        }

        composable("setting") {
            SettingsScreen(navController)
        }

        composable("myprofile") {
            MyProfileScreen(navController)
        }

        composable("friends") {
            FriendsScreen(navController)
        }

        composable("newsfeed") {
            NewsFeedScreen(navController)
        }
//        composable("chat/{userId}") { backStackEntry ->
//            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
//            ChatScreen(partnerId = userId)
//        }

    }
}