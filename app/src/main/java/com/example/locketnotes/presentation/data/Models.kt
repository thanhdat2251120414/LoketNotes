// Models.kt
package com.example.locketnotes.presentation.data

data class User(
    val id: String,
    val name: String,
    val avatar: String,
    val isOnline: Boolean = false
)

data class Message(
    val id: String,
    val sender: User,
    val content: String,
    val timestamp: String,
    val isActive: Boolean = false
)

data class StoryItem(
    val id: String,
    val user: User,
    val title: String
)