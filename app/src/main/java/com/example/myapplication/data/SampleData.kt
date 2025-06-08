// SampleData.kt
package com.example.myapplication.data

object SampleData {

    val storyItems = listOf(
        StoryItem(
            id = "1",
            user = User("1", "Your note", "YN"),
            title = "Your note"
        ),
        StoryItem(
            id = "2",
            user = User("2", "Thành Đạt", "TD", isOnline = true),
            title = "Thành Đạt"
        )
    )

    val messages = listOf(
        Message(
            id = "1",
            sender = User("3", "Kim Thị Ngọc Trâm", "KT"),
            content = "",
            timestamp = "Sent 23m ago"
        ),
        Message(
            id = "2",
            sender = User("2", "Thanh Đạt", "TD", isOnline = true),
            content = "",
            timestamp = "Active now",
            isActive = true
        ),
        Message(
            id = "3",
            sender = User("4", "Duy An", "DA"),
            content = "",
            timestamp = "Active 1h ago"
        )
    )

    val suggestions = listOf(
        User("5", "TyIII James", "TJ"),
        User("6", "MR.DND", "MD")
    )
}