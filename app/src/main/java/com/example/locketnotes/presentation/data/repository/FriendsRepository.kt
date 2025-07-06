package com.example.locketnotes.presentation.data.repository
import com.example.locketnotes.presentation.domain.model.Friend
import com.example.locketnotes.presentation.domain.model.FriendRequest
import com.example.locketnotes.presentation.domain.model.UserData
import kotlinx.coroutines.flow.Flow

interface FriendsRepository {
    // các hàm đã có...
    suspend fun searchUsers(query: String): Result<List<UserData>>
    suspend fun sendFriendRequest(userId: String): Result<Boolean>
    suspend fun hasSentFriendRequests(): Result<List<FriendRequest>>
    suspend fun getFriendRequests(): Result<List<FriendRequest>>
    suspend fun acceptFriendRequest(requestId: String): Result<Boolean>
    suspend fun declineFriendRequest(requestId: String): Result<Boolean>
    suspend fun getFriends(): Result<List<Friend>>
    suspend fun removeFriend(friendId: String): Result<Boolean>
    suspend fun blockUser(userId: String): Result<Boolean>

    fun observeFriendRequests(): Flow<List<FriendRequest>>
    fun observeFriends(): Flow<List<Friend>>
}
