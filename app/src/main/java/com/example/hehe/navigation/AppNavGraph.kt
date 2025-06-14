package com.example.hehe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hehe.screens.login.LoginScreen
import com.example.hehe.screens.newsfeed.FeedScreen
import com.example.hehe.screens.register.RegisterScreen
import com.example.hehe.model.PostData
import com.example.hehe.R
import androidx.compose.foundation.lazy.rememberLazyListState


@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("newsfeed") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegister = { email, password ->
                    // TODO: xử lý logic đăng ký ở đây nếu cần
                    // Sau khi đăng ký xong, chuyển về trang login
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("newsfeed") {
            val listState = rememberLazyListState()
            FeedScreen(
                posts = listOf(
                    PostData(R.drawable.ic_unity, "Unity", "Amazing!", R.drawable.building_1, 128),
                    PostData(R.drawable.ic_save, "Memory", "Kỷ niệm đẹp", R.drawable.building_2, 95),
                ),
                listState = listState
            )
        }
    }
}
