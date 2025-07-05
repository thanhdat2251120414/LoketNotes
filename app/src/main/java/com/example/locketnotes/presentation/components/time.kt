package com.example.locketnotes.presentation.components

import java.text.SimpleDateFormat
import java.util.*

fun formatRequestTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}