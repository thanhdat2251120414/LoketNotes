package com.example.locketnotes.presentation.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locketnotes.presentation.viewmodel.PostViewModel
import com.example.locketnotes.presentation.friends.FriendsViewModel
import com.example.locketnotes.presentation.components.PostItem
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import com.example.locketnotes.presentation.components.BottomNavBar
import com.example.locketnotes.presentation.components.TopBar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun NewsFeedScreen(
    navController: NavController,
    viewModel: PostViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel()
) {
    val posts = viewModel.posts
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    val friendIds by friendsViewModel.friendIds.collectAsState()

    // Load bài viết khi danh sách bạn bè được cập nhật
    LaunchedEffect(friendIds) {
        viewModel.loadPosts(currentUserId = currentUserId, friendIds = friendIds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar
        TopBar(centerText = "LoketNotes", navController = navController)

        // Post List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(posts) { post ->
                PostItem(post = post, currentUserId = currentUserId)
            }
        }

        // Bottom Navigation Bar
        BottomNavBar(navController = navController)
    }
}