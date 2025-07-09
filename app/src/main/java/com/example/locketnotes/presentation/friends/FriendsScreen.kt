package com.example.locketnotes.presentation.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.locketnotes.presentation.components.*

import com.example.locketnotes.presentation.domain.model.Friend

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = viewModel(),
    onNavigateToProfile: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showRemoveDialog by remember { mutableStateOf<Friend?>(null) }

    Scaffold(
        bottomBar = { BottomNavBar(navController) } // ✅ BottomNavBar cố định dưới cùng
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bạn bè",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = {
                        viewModel.loadFriends()
                        viewModel.loadFriendRequests()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    viewModel.searchUsers(query)
                },
                isSearching = uiState.isSearching
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Friend Requests Section
                    if (uiState.friendRequests.isNotEmpty()) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Lời mời kết bạn",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge {
                                    Text(uiState.friendRequests.size.toString())
                                }
                            }
                        }

                        items(uiState.friendRequests) { request ->
                            FriendRequestItem(
                                request = request,
                                onAccept = { viewModel.acceptFriendRequest(request.id) },
                                onDecline = { viewModel.declineFriendRequest(request.id) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }

                    // Search Results Section
                    if (uiState.searchResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Kết quả tìm kiếm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        items(uiState.searchResults) { user ->
                            UserSearchItem(
                                user = user,
                                onSendRequest = { viewModel.sendFriendRequest(user.userId) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }

                    // Friends List Section
                    if (uiState.friends.isNotEmpty()) {
                        item {
                            Text(
                                text = "Danh sách bạn bè",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        items(uiState.friends) { friend ->
                            FriendItem(
                                friend = friend,
                                onFriendClick = { onNavigateToProfile(friend.id) },
                                onRemoveFriend = { showRemoveDialog = friend }
                            )
                        }
                    }
                }
            }
        }

        // Remove Friend Dialog
        showRemoveDialog?.let { friend ->
            AlertDialog(
                onDismissRequest = { showRemoveDialog = null },
                title = { Text("Xóa bạn bè?") },
                text = { Text("Bạn có chắc muốn xóa ${friend.user.username} khỏi danh sách bạn bè?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeFriend(friend.id)
                        showRemoveDialog = null
                    }) {
                        Text("Xóa", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveDialog = null }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
