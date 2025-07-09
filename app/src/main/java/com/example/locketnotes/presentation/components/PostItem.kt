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

    // Load like data from Firebase
    LaunchedEffect(post.id) {
        val db = FirebaseDatabase.getInstance()
        val photoRef = db.getReference("user/${post.userId}/photos/${post.id}")

        photoRef.child("likes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                likeCount = snapshot.getValue(Int::class.java) ?: 0
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostItem", "Failed to load like count", error.toException())
            }
        })

        photoRef.child("likedBy").child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLiked = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PostItem", "Failed to check likedBy", error.toException())
            }
        })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {

            // Header: avatar + name + time
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userAvatar,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(Date(post.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Post Image full width
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // makes it square
                    .clip(RoundedCornerShape(0.dp))
            )

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isLiked = !isLiked
                    if (isLiked) {
                        incrementLike(post.userId, post.id, currentUserId)
                    } else {
                        decrementLike(post.userId, post.id, currentUserId)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Black
                    )
                }

                IconButton(onClick = {
                    showUsersWhoLiked(post.userId, post.id)
                }) {
                    Icon(Icons.Default.ChatBubble, contentDescription = "Comments", tint = Color.Black)
                }

                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
                }
            }

            // Like count
            if (likeCount > 0) {
                Text(
                    text = "$likeCount lượt thích",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color.Black
                )
            }

            // Caption
            if (post.message.isNotBlank()) {
                Text(
                    text = post.message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
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


