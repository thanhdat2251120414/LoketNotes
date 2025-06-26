package com.example.locketnotes.presentation.domain.model

data class UserData(
    val userId: String,
    val username: String = "",
    val email: String = "",
)