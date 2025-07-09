package com.example.locketnotes.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TopBar(centerText: String = "Loket Notes", navController: NavController) {
    var avatarUrl by remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var avatarPressed by remember { mutableStateOf(false) }
    var messagePressed by remember { mutableStateOf(false) }

    // Lấy avatar từ Firebase
    LaunchedEffect(userId) {
        userId?.let {
            val dbRef = FirebaseDatabase.getInstance().getReference("user").child(it)
            dbRef.child("profileImageUrl").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    avatarUrl = snapshot.getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    val avatarScale by animateFloatAsState(targetValue = if (avatarPressed) 0.9f else 1f, label = "avatarScale")
    val messageScale by animateFloatAsState(targetValue = if (messagePressed) 0.9f else 1f, label = "messageScale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar người dùng
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(44.dp)
                .scale(avatarScale)
                .clip(CircleShape)
                .background(Color(0xFFF2F2F2))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    avatarPressed = true
                    navController.navigate("myprofile") {
                        launchSingleTop = true
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(150)
                        avatarPressed = false
                    }
                },
            contentScale = ContentScale.Crop
        )

        // Logo "Loket Notes" có hiệu ứng xuất hiện
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = centerText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Biểu tượng tin nhắn
        Icon(
            imageVector = Icons.Default.Message,
            contentDescription = "Tin nhắn",
            tint = Color.Black,
            modifier = Modifier
                .size(30.dp)
                .scale(messageScale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    messagePressed = true
                    navController.navigate("chat") {
                        launchSingleTop = true
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(150)
                        messagePressed = false
                    }
                }
        )
    }
}
