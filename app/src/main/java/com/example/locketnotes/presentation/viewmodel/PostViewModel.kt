package com.example.locketnotes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.example.locketnotes.presentation.domain.model.Post
import androidx.compose.runtime.mutableStateListOf
import android.util.Log


class PostViewModel : ViewModel() {
    private val _posts = mutableStateListOf<Post>()
    val posts: List<Post> = _posts

    private val dbRef = FirebaseDatabase.getInstance().getReference("user")

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _posts.clear()
                for (userSnap in snapshot.children) {
                    val username = userSnap.child("username").getValue(String::class.java) ?: ""
                    val avatarUrl = userSnap.child("profileImageUrl").getValue(String::class.java) ?: ""
                    val userId = userSnap.key ?: ""

                    val photosSnap = userSnap.child("photos")
                    for (photoSnap in photosSnap.children) {
                        val photoId = photoSnap.key ?:""
                        val imageUrl = photoSnap.child("imageUrl").getValue(String::class.java) ?: ""
                        val message = photoSnap.child("message").getValue(String::class.java) ?: ""
                        val timestamp = photoSnap.child("timestamp").getValue(Long::class.java) ?: 0L
                        val likes = photoSnap.child("likes").getValue(Int::class.java) ?: 0

                        _posts.add(
                            Post(
                                id = photoId,
                                userId = userId,
                                username = username,
                                userAvatar = avatarUrl,
                                imageUrl = imageUrl,
                                message = message,
                                timestamp = timestamp,
                                likes = likes
                            )
                        )
                    }
                }

                _posts.sortByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                // handle error
            }
        })
    }
}
