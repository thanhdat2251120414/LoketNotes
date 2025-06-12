package com.example.locketnotes.presentation.components


import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun BottomNavBar() {
    BottomNavigation(
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.ArrowBack, contentDescription = "Back") },
            selected = false,
            onClick = { /* back action */ },
            label = { Text("Back") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = true,
            onClick = { /* home */ },
            label = { Text("Home") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Article, contentDescription = "News") },
            selected = false,
            onClick = { /* go to news */ },
            label = { Text("News") }
        )
    }
}
