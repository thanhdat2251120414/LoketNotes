package com.example.locketnotes.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.locketnotes.presentation.domain.model.Message
import com.example.locketnotes.presentation.domain.model.MessageType
import com.example.locketnotes.presentation.domain.model.UserData

@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    chatPartner: UserData?
) {
    val backgroundColor = if (isFromCurrentUser) Color(0xFF00BCD4) else Color(0xFFF0F0F0)
    val textColor = if (isFromCurrentUser) Color.White else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromCurrentUser) {
            AsyncImage(
                model = chatPartner?.profileImageUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))
        } else {
            Spacer(modifier = Modifier.width(36.dp)) // giữ khoảng trống bên trái
        }

        Column(
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
        ) {
            Card(
                modifier = Modifier.widthIn(max = 260.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    when (message.messageType) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.content,
                                color = textColor,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        }

                        MessageType.IMAGE -> {
                            message.fileUrl?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "Image message",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 180.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        else -> {
                            Text(
                                text = message.content,
                                color = textColor,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(message.timestamp),
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )

                        if (isFromCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = null,
                                tint = if (message.isRead) Color.Blue else textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

