package com.example.locketnotes.presentation.domain.model

data class PhotoUpload(
    val id: String = "",
    val imageUrl: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",
    val isPublic: Boolean = false
)

