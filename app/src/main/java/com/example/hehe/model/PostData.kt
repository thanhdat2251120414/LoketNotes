package com.example.hehe.model

data class PostData(
    val avatarId: Int,
    val name: String,
    val content: String,
    val imageId: Int?,
    val likesCount: Int
)
