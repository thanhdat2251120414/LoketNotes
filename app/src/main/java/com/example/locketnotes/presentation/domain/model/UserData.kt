package com.example.locketnotes.presentation.domain.model

// Thông tin tài khoản người dùng

data class UserData(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val gender: String? = null,
    val birthDate: Long? = null,
    val profileImageUrl: String? = null,
    val isRequested: Boolean = false,
    val requestStatus: RequestStatus? = null,
    val lastSeen: Long = 0L
)

// Yêu cầu kết bạn gửi/nhận

data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val senderProfileImage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING
)

// Trạng thái lời mời

enum class RequestStatus {
    PENDING, ACCEPTED, DECLINED
}

// Quan hệ bạn bè (Friend)

data class Friend(
    val id: String = "",
    val user: UserData = UserData(),
    val addedDate: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val lastMessage: String? = null
)

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val messageType: MessageType = MessageType.TEXT,
    val fileUrl: String? = null,
    val fileName: String? = null
)

// Message type enum
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    FILE,
    AUDIO
}

// Chat info model
data class ChatInfo(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSender: String = "",
    val unreadCount: Map<String, Int> = emptyMap()
)
