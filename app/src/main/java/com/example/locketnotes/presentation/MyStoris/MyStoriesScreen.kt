package com.example.locketnotes.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.locketnotes.presentation.MyStoris.StoryViewModel
import com.example.locketnotes.presentation.components.BottomNavBar
import com.example.locketnotes.presentation.components.StoryGrid
import com.example.locketnotes.presentation.components.TopBar
import com.example.locketnotes.presentation.domain.model.Story

@Composable
fun MyStoriesScreen(
    navController: NavController,
    viewModel: StoryViewModel = viewModel()
) {
    Scaffold(
        topBar = { TopBar(centerText = "My story", navController = navController) },
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(onClick = { addNewStory(viewModel) }) {
                Icon(Icons.Default.Add, contentDescription = "Add story")
            }
        },
        content = { padding ->
            StoryGrid(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                viewModel = viewModel
            )
        }
    )
}

// Hàm thêm Story test
fun addNewStory(viewModel: StoryViewModel) {
    val newStory = Story(
        id = "", // Firebase sẽ tự gán
        imageUrl = "https://picsum.photos/300",
        message = "Test story at ${System.currentTimeMillis()}",
        timestamp = System.currentTimeMillis(),
        userId = "testUser"
    )
    viewModel.addStory(newStory)
}