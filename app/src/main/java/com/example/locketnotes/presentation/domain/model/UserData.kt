package com.example.locketnotes.presentation.domain.model

data class UserData(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val gender: String? = null,
    val birthDate: Long? = null,
    val profileImageUrl: String? = "https://res.cloudinary.com/dtzum2dzo/image/upload/v1751774560/OIP_ryvkhk.jpg"
)
/*data class User(
    val id: String = "",
    val username: String = "",
   // val displayName: String = "",
    val profileImageUrl: String = "",
    val email: String = ""
)*/

data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val senderProfileImage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING
)

enum class RequestStatus {
    PENDING, ACCEPTED, DECLINED
}
data class Friend(
    val id: String = "",
    val user: UserData = UserData(),
    val addedDate: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
)
