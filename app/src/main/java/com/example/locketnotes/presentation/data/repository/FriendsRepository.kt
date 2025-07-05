package com.example.locketnotes.presentation.data.repository

import com.example.locketnotes.presentation.domain.model.Friend
import com.example.locketnotes.presentation.domain.model.FriendRequest
import com.example.locketnotes.presentation.domain.model.UserData


interface FriendsRepository {
    suspend fun searchUsers(query: String): Result<List<UserData>>
    suspend fun sendFriendRequest(userId: String): Result<Boolean>
    suspend fun getFriendRequests(): Result<List<FriendRequest>>
    suspend fun acceptFriendRequest(requestId: String): Result<Boolean>
    suspend fun declineFriendRequest(requestId: String): Result<Boolean>
    suspend fun getFriends(): Result<List<Friend>>
    suspend fun removeFriend(friendId: String): Result<Boolean>
    suspend fun blockUser(userId: String): Result<Boolean>
}