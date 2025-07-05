package com.example.locketnotes.presentation.data.repository

import com.example.locketnotes.presentation.domain.model.Friend
import com.example.locketnotes.presentation.domain.model.FriendRequest
import com.example.locketnotes.presentation.domain.model.UserData
import kotlinx.coroutines.delay
class FriendsRepositoryImpl : FriendsRepository {

    override suspend fun searchUsers(query: String): Result<List<UserData>> {
        return try {
            delay(500)
            val mockUsers = listOf(
                UserData(
                    userId = "1",
                    username = "user1",
                    email = "nguyenvana@example.com",
                    profileImageUrl = "https://example.com/avatar1.jpg"
                ),
                UserData(
                    userId = "2",
                    username = "user2",
                    email = "nguyenvana@example.com",
                    profileImageUrl = "https://example.com/avatar1.jpg"
                ),
                UserData(
                    userId = "3",
                    username = "user3",
                    email = "nguyenvana@example.com",
                    profileImageUrl = "https://example.com/avatar1.jpg"
                )
            ).filter {
                it.username.contains(query, ignoreCase = true) ||
                        it.username.contains(query, ignoreCase = true)
            }
            Result.success(mockUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendFriendRequest(userId: String): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriendRequests(): Result<List<FriendRequest>> {
        return try {
            delay(500)
            val mockRequests = listOf(
                FriendRequest("req1", "user4", "currentUser", "Phạm Văn D", "https://example.com/avatar4.jpg"),
                FriendRequest("req2", "user5", "currentUser", "Hoàng Thị E", "https://example.com/avatar5.jpg")
            )
            Result.success(mockRequests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriends(): Result<List<Friend>> {
        return try {
            delay(500)
            val mockFriends = listOf(
                Friend("f1", UserData("1", "user1", "Nguyễn Văn A", "https://example.com/avatar1.jpg"), isOnline = true),
                Friend("f2", UserData("2", "user2", "Trần Thị B", "https://example.com/avatar2.jpg"), isOnline = false, lastSeen = System.currentTimeMillis() - 3600000),
                Friend("f3", UserData("3", "user3", "Lê Văn C", "https://example.com/avatar3.jpg"), isOnline = true)
            )
            Result.success(mockFriends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFriend(friendId: String): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun blockUser(userId: String): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
