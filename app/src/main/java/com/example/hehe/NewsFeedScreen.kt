package com.example.hehe.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hehe.R
import com.example.hehe.ui.components.PostItem

@Composable
fun NewsFeedScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(R.drawable.ic_react),
                contentDescription = "Logo",
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "News feed",
                style = MaterialTheme.typography.titleLarge // ✅ Sửa ở đây
            )

            Image(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = "Chat",
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Post Item
        PostItem(
            avatarId = R.drawable.ic_unity,
            name = "Unity Team",
            content = "Amazing 3D engine",
            imageId = null,
            likesCount = 128,
            onLike = { Toast.makeText(context, "Liked!", Toast.LENGTH_SHORT).show() },
            onComment = { Toast.makeText(context, "Comment!", Toast.LENGTH_SHORT).show() },
            onShare = { Toast.makeText(context, "Shared!", Toast.LENGTH_SHORT).show() },
            onSave = { Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show() }
        )
    }
}
