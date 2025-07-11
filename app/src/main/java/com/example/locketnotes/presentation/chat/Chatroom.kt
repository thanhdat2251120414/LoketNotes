package com.example.locketnotes.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.locketnotes.presentation.domain.model.Message
import com.example.locketnotes.presentation.domain.model.MessageType
import com.example.locketnotes.presentation.domain.model.UserData
import kotlinx.coroutines.delay
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    partnerId: String,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val chatPartner by viewModel.chatPartner.collectAsState()
    val error by viewModel.error.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val partner = chatPartner

    // Typing logic
    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty()) {
            viewModel.setTypingStatus(partnerId, true)
            delay(1000)
            viewModel.setTypingStatus(partnerId, false)
        }
    }

    // Init chat
    LaunchedEffect(partnerId) {
        viewModel.initChat(partnerId)
    }

    // Scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = partner?.profileImageUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = partner?.username ?: "Loading...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            when {
                                isTyping -> Text(
                                    text = "typing...",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontStyle = FontStyle.Italic
                                )
                                partner?.lastSeen != null && partner.lastSeen > 0 -> Text(
                                    text = "Last seen ${formatLastSeen(partner.lastSeen)}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video call", tint = Color.White)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice call", tint = Color.White)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00BCD4))
            )
        },
        bottomBar = {
            MessageInputBar(
                message = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.trim().isNotEmpty()) {
                        viewModel.sendMessage(messageText, partnerId)
                        messageText = ""
                    }
                },
                onImageClick = { imageUri ->
                    viewModel.sendImageMessage(imageUri, partnerId)
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00BCD4))
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                currentUser?.let { user ->
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = message.senderId == user.userId,
                            chatPartner = chatPartner
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}
