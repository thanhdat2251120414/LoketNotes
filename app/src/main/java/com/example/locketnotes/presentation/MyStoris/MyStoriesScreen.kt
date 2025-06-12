package com.example.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.locketnotes.presentation.components.BottomNavBar
import com.example.locketnotes.presentation.components.StoryGrid
import com.example.locketnotes.presentation.components.TopBar


@Composable
fun MyStoriesScreen(navController: NavController) {
    Scaffold(
        topBar = { TopBar(centerText = "My story", navController = navController) },
        bottomBar = { BottomNavBar() },
        content = { padding ->
            StoryGrid(modifier = Modifier.padding(padding))
        }
    )
}

