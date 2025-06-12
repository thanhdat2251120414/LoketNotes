// Components.kt
package com.example.locketnotes.presentation.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locketnotes.presentation.data.Message
import com.example.locketnotes.presentation.data.StoryItem
import com.example.locketnotes.presentation.data.User


//import com.example.myapplication.data.Message
//import com.example.locketnote.data.*
//import com.example.myapplication.data.StoryItem
//import com.example.myapplication.data.User

@Composable
fun ProfileAvatar(
    avatar: String,
    size: Int = 56,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for avatar
            Text(
                text = avatar,
                color = Color.White,
                fontSize = (size / 3).sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Green)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun StoryItemComponent(
    storyItem: StoryItem,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(80.dp)
    ) {
        ProfileAvatar(
            avatar = storyItem.user.avatar,
            size = 56,
            isOnline = storyItem.user.isOnline
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = storyItem.title,
            fontSize = 12.sp,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MessageItemComponent(
    message: Message,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(
            avatar = message.sender.avatar,
            size = 56,
            isOnline = message.sender.isOnline
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = message.sender.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Text(
                text = message.timestamp,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Camera icon placeholder
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
            contentDescription = "Camera",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SuggestionItemComponent(
    user: User,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(
            avatar = user.avatar,
            size = 56
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Verified icon
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                    contentDescription = "Verified",
                    tint = Color.Blue,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "Tap to chat",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Camera icon placeholder
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
            contentDescription = "Camera",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}