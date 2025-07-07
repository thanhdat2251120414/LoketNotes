package com.example.locketnotes.presentation.domain.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val imageUrl: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0
)


