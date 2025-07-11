package com.example.locketnotes.presentation.chat

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

@Composable
fun ChatApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "messenger"
    ) {
        // Màn hình danh sách bạn bè/chat
        composable("messenger") {
            MessengerScreen(
                navController = navController,
                onChatClick = { partnerId ->
                    navController.navigate("chat/$partnerId")
                }
            )
        }

        // Màn hình chat chi tiết
        composable(
            route = "chat/{partnerId}",
            arguments = listOf(navArgument("partnerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId")
            if (!partnerId.isNullOrBlank()) {
                ChatScreen(
                    partnerId = partnerId,
                    navController = navController
                )
            } else {
                // Trường hợp lỗi nếu không có partnerId
                // Có thể hiển thị Snackbar hoặc quay lại Messenger
                navController.popBackStack()
            }
        }
    }
}
