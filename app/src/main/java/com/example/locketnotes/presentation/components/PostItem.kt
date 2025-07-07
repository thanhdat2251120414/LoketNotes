package com.example.locketnotes.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.locketnotes.presentation.domain.model.Post
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostItem(post: Post, currentUserId: String) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(post.likes) }
    val context = LocalContext.current

    // Load trạng thái like và số lượt like từ Firebase
    LaunchedEffect(post.id) {
        val db = FirebaseDatabase.getInstance()
        val photoRef = db.getReference("user").child(post.userId).child("photos").child(post.id)

        // Load like count
        val likesRef = photoRef.child("likes")
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                likeCount = snapshot.getValue(Int::class.java) ?: 0
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostItem", "Failed to load like count", error.toException())
            }
        })

        // Check if user already liked
        val likedByRef = photoRef.child("likedBy").child(currentUserId)
        likedByRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLiked = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostItem", "Failed to check likedBy", error.toException())
            }
        })
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.userAvatar,
                contentDescription = "User Avatar",
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(post.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(post.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Image
        AsyncImage(
            model = post.imageUrl,
            contentDescription = "Post Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Like Count
        Text(
            text = "$likeCount lượt thích",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.Gray
        )

        // Caption
        Text(
            text = post.message,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like / Unlike
            IconButton(
                onClick = {
                    if (!isLiked) {
                        isLiked = true
                        incrementLike(post.userId, post.id, currentUserId)
                    } else {
                        isLiked = false
                        decrementLike(post.userId, post.id, currentUserId)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }

            // View likers
            IconButton(onClick = {
                showUsersWhoLiked(post.userId, post.id)
            }) {
                Icon(Icons.Default.ChatBubble, contentDescription = "View Likers", tint = Color.Gray)
            }

            IconButton(onClick = { /* TODO: Share */ }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Gray)
            }
        }

        if (isLiked) {
            Text(
                text = "Bạn đã thích bài viết này ❤️",
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red
            )
        }
    }
}



// Like Increment Function
fun incrementLike(postOwnerId: String, photoId: String, currentUserId: String) {
    val ref = FirebaseDatabase.getInstance()
        .getReference("user/$postOwnerId/photos/$photoId")

    ref.child("likedBy").child(currentUserId).setValue(true)
    ref.child("likes").runTransaction(object : Transaction.Handler {
        override fun doTransaction(currentData: MutableData): Transaction.Result {
            val currentLikes = currentData.getValue(Int::class.java) ?: 0
            currentData.value = currentLikes + 1
            return Transaction.success(currentData)
        }

        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
            if (error != null) {
                Log.e("Like", "Increment failed", error.toException())
            }
        }
    })
}

fun decrementLike(postOwnerId: String, photoId: String, currentUserId: String) {
    val ref = FirebaseDatabase.getInstance()
        .getReference("user/$postOwnerId/photos/$photoId")

    ref.child("likedBy").child(currentUserId).removeValue()
    ref.child("likes").runTransaction(object : Transaction.Handler {
        override fun doTransaction(currentData: MutableData): Transaction.Result {
            val currentLikes = currentData.getValue(Int::class.java) ?: 0
            currentData.value = (currentLikes - 1).coerceAtLeast(0)
            return Transaction.success(currentData)
        }

        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
            if (error != null) {
                Log.e("Like", "Decrement failed", error.toException())
            }
        }
    })
}

fun showUsersWhoLiked(postOwnerId: String, photoId: String) {
    val ref = FirebaseDatabase.getInstance()
        .getReference("user/$postOwnerId/photos/$photoId/likedBy")

    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val users = mutableListOf<String>()
            for (child in snapshot.children) {
                users.add(child.key ?: "")
            }
            Log.d("LikedBy", "Người đã thích: ${users.joinToString()}")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("LikedBy", "Failed to read liked users", error.toException())
        }
    })
}


