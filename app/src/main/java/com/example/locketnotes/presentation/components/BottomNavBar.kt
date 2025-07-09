package com.example.locketnotes.presentation.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "newsfeed"),
        BottomNavItem("Looking", Icons.Default.Search, "friends"),
        BottomNavItem("Camera", Icons.Default.AddCircle, "camera"),
        BottomNavItem("MyStory", Icons.Default.Movie, "MyStories"),
        BottomNavItem("Settings", Icons.Default.Settings, "setting")
    )

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black,
        elevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("newsfeed") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                selectedContentColor = Color(0xFF2196F3),
                unselectedContentColor = Color.Gray
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)
