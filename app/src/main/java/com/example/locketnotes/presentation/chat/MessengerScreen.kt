package com.example.locketnotes.presentation.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.locketnotes.presentation.components.BottomNavBar
import com.example.locketnotes.presentation.viewmodel.MessengerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(
    navController: NavHostController,
    onChatClick: (String) -> Unit,
    viewModel: MessengerViewModel = hiltViewModel()
) {
    val friends by viewModel.friends.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Messenger",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.Black,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("camera") {
                            popUpTo("chat") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Compose */ }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Compose",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: $error",
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                friends.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You have no friends yet.", color = Color.Gray)
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Messages", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("Requests", fontSize = 16.sp, color = Color.Gray)
                            }
                        }

                        items(friends) { friend ->
                            MessageItemComponent(
                                username = friend.user.username,
                                avatar = friend.user.username.firstOrNull()?.toString() ?: "?",
                                lastMessage = friend.lastMessage ?: "",
                                isOnline = friend.isOnline,
                                onClick = {
                                    onChatClick(friend.user.userId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
