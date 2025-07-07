package com.example.locketnotes.presentation.domain.model

data class PhotoUpload(
    val id: String = "",
    val imageUrl: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val userId: String = "",
    val isPublic: Boolean = false,
    val likes: Int = 0
)


