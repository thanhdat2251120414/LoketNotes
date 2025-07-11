package com.example.locketnotes.presentation.domain.model

data class Story(
    val id: String = "",
    val imageUrl: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val userId: String = ""
)
{
    constructor() : this("", "", "", 0L, "")
}