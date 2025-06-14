package com.example.hehe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.hehe.R



@Composable
fun PostItem(
    avatarId: Int,
    name: String,
    content: String,
    imageId: Int?,
    onLike: () -> Unit,
    likesCount: Int,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = avatarId),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = name, fontSize = 16.sp, color = Color.Black)
                Text(text = "2 hours ago", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ⬇️ Ảnh TRÀN VIỀN
        imageId?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                IconButton(
                    onClick = onLike,
                    modifier = Modifier.size(40.dp) // Nhỏ gọn hơn
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart),
                        contentDescription = "Like",
                        modifier = Modifier.size(25.dp), // Kích thước icon nhỏ
                        tint = Color.Black
                    )
                }

                IconButton(onClick = onComment,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comment),
                        contentDescription = "Comment",
                        modifier = Modifier.size(25.dp)
                    )
                }
                IconButton(onClick = onShare,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = "Share",
                        modifier = Modifier.size(25.dp))
                }
            }
            IconButton(onClick = onSave,
                modifier = Modifier.size(40.dp)
                ) {
                Icon(painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = "Save",
                    modifier = Modifier.size(25.dp))
            }
        }

        // Content
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(text = "$likesCount likes", fontSize = 14.sp, color = Color.Black)
            Text(text = content, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}


