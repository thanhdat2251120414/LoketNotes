package com.example.locketnotes.presentation.data.repository

import com.example.locketnotes.presentation.domain.model.Friend
import com.example.locketnotes.presentation.domain.model.FriendRequest
import com.example.locketnotes.presentation.domain.model.UserData
import com.example.locketnotes.presentation.domain.model.RequestStatus
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class FriendsRepositoryImpl @Inject constructor() : FriendsRepository {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val usersRef = database.reference.child("user")
    private val friendRequestsRef = database.reference.child("friendRequests")
    private val friendsRef = database.reference.child("friends")
    private val blockedUsersRef = database.reference.child("blockedUsers")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    override suspend fun searchUsers(query: String): Result<List<UserData>> {
        return try {
            suspendCancellableCoroutine { continuation ->
                usersRef.orderByChild("email")
                    .startAt(query.lowercase())
                    .endAt(query.lowercase() + "\uf8ff")
                    .limitToFirst(20)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val users = mutableListOf<UserData>()

                            snapshot.children.forEach { userSnapshot ->
                                val user = userSnapshot.getValue(UserData::class.java)
                                val userId = userSnapshot.key

                                if (user != null && userId != null && userId != currentUserId) {
                                    users.add(user.copy(userId = userId))
                                }
                            }

                            // Bước 2: lấy tất cả friend requests liên quan
                            friendRequestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(requestsSnapshot: DataSnapshot) {
                                    val sentMap = mutableMapOf<String, RequestStatus>()
                                    val receivedMap = mutableMapOf<String, RequestStatus>()

                                    requestsSnapshot.children.forEach {
                                        val request = it.getValue(FriendRequest::class.java) ?: return@forEach
                                        if (request.senderId == currentUserId) {
                                            sentMap[request.receiverId] = request.status
                                        } else if (request.receiverId == currentUserId) {
                                            receivedMap[request.senderId] = request.status
                                        }
                                    }

                                    // Bước 3: lấy danh sách bạn bè
                                    friendsRef.child(currentUserId)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(friendsSnapshot: DataSnapshot) {
                                                val friendIds = friendsSnapshot.children.mapNotNull {
                                                    val friend = it.getValue(Friend::class.java)
                                                    friend?.user?.userId
                                                }.toSet()

                                                // Bước 4: gán requestStatus cho từng user
                                                val updatedUsers = users.map { user ->
                                                    val id = user.userId
                                                    val status = when {
                                                        friendIds.contains(id) -> RequestStatus.ACCEPTED
                                                        sentMap.containsKey(id) -> sentMap[id]
                                                        receivedMap.containsKey(id) -> receivedMap[id]
                                                        else -> null
                                                    }
                                                    user.copy(requestStatus = status)
                                                }

                                                continuation.resume(Result.success(updatedUsers))
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                continuation.resume(Result.failure(Exception(error.message)))
                                            }
                                        })
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    continuation.resume(Result.failure(Exception(error.message)))
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Result.failure(Exception(error.message)))
                        }
                    })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    override suspend fun sendFriendRequest(userId: String): Result<Boolean> {
        return try {
            suspendCancellableCoroutine { continuation ->
                // Check if friend request already exists

                friendRequestsRef
                    .orderByChild("senderId")
                    .equalTo(currentUserId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var requestExists = false

                            snapshot.children.forEach { requestSnapshot ->
                                val request = requestSnapshot.getValue(FriendRequest::class.java)
                                if (request?.receiverId == userId && request.status == RequestStatus.PENDING) {
                                    requestExists = true
                                }
                            }

                            if (requestExists) {
                                continuation.resume(Result.failure(Exception("Friend request already sent")))
                                return
                            }

                            // Get current user data
                            usersRef.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val currentUser = userSnapshot.getValue(UserData::class.java)

                                    if (currentUser != null) {
                                        val requestId = friendRequestsRef.push().key ?: ""

                                        val friendRequest = FriendRequest(
                                            id = requestId,
                                            senderId = currentUserId,
                                            receiverId = userId,
                                            senderName = currentUser.username,
                                            senderProfileImage = currentUser.profileImageUrl ?: "",
                                            timestamp = System.currentTimeMillis(),
                                            status = RequestStatus.PENDING
                                        )

                                        friendRequestsRef.child(requestId).setValue(friendRequest)
                                            .addOnSuccessListener {
                                                continuation.resume(Result.success(true))
                                            }
                                            .addOnFailureListener { error ->
                                                continuation.resume(Result.failure(error))
                                            }
                                    } else {
                                        continuation.resume(Result.failure(Exception("Current user not found")))
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    continuation.resume(Result.failure(Exception(error.message)))
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Result.failure(Exception(error.message)))
                        }
                    })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun hasSentFriendRequests(): Result<List<FriendRequest>> {
        return try {
            suspendCancellableCoroutine { continuation ->
                friendRequestsRef
                    .orderByChild("senderId")
                    .equalTo(currentUserId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val requests = snapshot.children.mapNotNull { snap ->
                                val request = snap.getValue(FriendRequest::class.java)
                                val requestId = snap.key
                                if (request != null && requestId != null && request.status == RequestStatus.PENDING) {
                                    request.copy(id = requestId)
                                } else null
                            }
                            continuation.resume(Result.success(requests))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Result.failure(Exception(error.message)))
                        }
                    })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getFriendRequests(): Result<List<FriendRequest>> {
        return try {
            suspendCancellableCoroutine { continuation ->
                friendRequestsRef
                    .orderByChild("receiverId")
                    .equalTo(currentUserId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val requests = mutableListOf<FriendRequest>()

                            snapshot.children.forEach { requestSnapshot ->
                                val request = requestSnapshot.getValue(FriendRequest::class.java)
                                val requestId = requestSnapshot.key

                                if (request != null && requestId != null && request.status == RequestStatus.PENDING) {
                                    requests.add(request.copy(id = requestId))
                                }
                            }

                            continuation.resume(Result.success(requests))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Result.failure(Exception(error.message)))
                        }
                    })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Boolean> {
        return try {
            suspendCancellableCoroutine { continuation ->
                // Get the friend request
                friendRequestsRef.child(requestId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(requestSnapshot: DataSnapshot) {
                        val request = requestSnapshot.getValue(FriendRequest::class.java)

                        if (request != null) {
                            // Get both users' data
                            usersRef.child(request.senderId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(senderSnapshot: DataSnapshot) {
                                    val senderUser = senderSnapshot.getValue(UserData::class.java)

                                    usersRef.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(receiverSnapshot: DataSnapshot) {
                                            val receiverUser = receiverSnapshot.getValue(UserData::class.java)

                                            if (senderUser != null && receiverUser != null) {
                                                // Create friend entries
                                                val friendId1 = friendsRef.push().key ?: ""
                                                val friendId2 = friendsRef.push().key ?: ""

                                                val friend1 = Friend(
                                                    id = friendId1,
                                                    user = senderUser.copy(userId = request.senderId),
                                                    addedDate = System.currentTimeMillis(),
                                                    isOnline = false
                                                )

                                                val friend2 = Friend(
                                                    id = friendId2,
                                                    user = receiverUser.copy(userId = currentUserId),
                                                    addedDate = System.currentTimeMillis(),
                                                    isOnline = false
                                                )

                                                // Batch update
                                                val updates = hashMapOf<String, Any>(
                                                    "friendRequests/$requestId/status" to RequestStatus.ACCEPTED.name,
                                                    "friends/$currentUserId/$friendId1" to friend1,
                                                    "friends/${request.senderId}/$friendId2" to friend2
                                                )

                                                database.reference.updateChildren(updates)
                                                    .addOnSuccessListener {
                                                        continuation.resume(Result.success(true))
                                                    }
                                                    .addOnFailureListener { error ->
                                                        continuation.resume(Result.failure(error))
                                                    }
                                            } else {
                                                continuation.resume(Result.failure(Exception("User data not found")))
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            continuation.resume(Result.failure(Exception(error.message)))
                                        }
                                    })
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    continuation.resume(Result.failure(Exception(error.message)))
                                }
                            })
                        } else {
                            continuation.resume(Result.failure(Exception("Friend request not found")))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Boolean> {
        return try {
            suspendCancellableCoroutine { continuation ->
                friendRequestsRef.child(requestId).child("status").setValue(RequestStatus.DECLINED.name)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(true))
                    }
                    .addOnFailureListener { error ->
                        continuation.resume(Result.failure(error))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFriends(): Result<List<Friend>> {
        return try {
            suspendCancellableCoroutine { continuation ->
                friendsRef.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val friends = mutableListOf<Friend>()

                        snapshot.children.forEach { friendSnapshot ->
                            val friend = friendSnapshot.getValue(Friend::class.java)
                            val friendId = friendSnapshot.key

                            if (friend != null && friendId != null) {
                                friends.add(friend.copy(id = friendId))
                            }
                        }

                        continuation.resume(Result.success(friends))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFriend(friendId: String): Result<Boolean> {
        return try {
            suspendCancellableCoroutine { continuation ->
                // Get the friend to remove
                friendsRef.child(currentUserId).child(friendId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(friendSnapshot: DataSnapshot) {
                        val friend = friendSnapshot.getValue(Friend::class.java)

                        if (friend != null) {
                            // Find corresponding friendship in other user's list
                            friendsRef.child(friend.user.userId).orderByChild("user/userId").equalTo(currentUserId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(otherSnapshot: DataSnapshot) {
                                        val updates = hashMapOf<String, Any?>()

                                        // Remove from current user's friends
                                        updates["friends/$currentUserId/$friendId"] = null

                                        // Remove from other user's friends
                                        otherSnapshot.children.forEach { otherFriendSnapshot ->
                                            val otherFriendId = otherFriendSnapshot.key
                                            if (otherFriendId != null) {
                                                updates["friends/${friend.user.userId}/$otherFriendId"] = null
                                            }
                                        }

                                        database.reference.updateChildren(updates)
                                            .addOnSuccessListener {
                                                continuation.resume(Result.success(true))
                                            }
                                            .addOnFailureListener { error ->
                                                continuation.resume(Result.failure(error))
                                            }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        continuation.resume(Result.failure(Exception(error.message)))
                                    }
                                })
                        } else {
                            continuation.resume(Result.failure(Exception("Friend not found")))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun blockUser(userId: String): Result<Boolean> {
        return try {
            suspendCancellableCoroutine { continuation ->
                val blockId = blockedUsersRef.push().key ?: ""

                val blockData = hashMapOf<String, Any>(
                    "blockedUserId" to userId,
                    "blockedDate" to System.currentTimeMillis()
                )

                blockedUsersRef.child(currentUserId).child(blockId).setValue(blockData)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(true))
                    }
                    .addOnFailureListener { error ->
                        continuation.resume(Result.failure(error))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Real-time observers
    override fun observeFriendRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val listener = friendRequestsRef
            .orderByChild("receiverId")
            .equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requests = mutableListOf<FriendRequest>()

                    snapshot.children.forEach { requestSnapshot ->
                        val request = requestSnapshot.getValue(FriendRequest::class.java)
                        val requestId = requestSnapshot.key

                        if (request != null && requestId != null && request.status == RequestStatus.PENDING) {
                            requests.add(request.copy(id = requestId))
                        }
                    }

                    trySend(requests)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(Exception(error.message))
                }
            })

        awaitClose { friendRequestsRef.removeEventListener(listener) }
    }

    override fun observeFriends(): Flow<List<Friend>> = callbackFlow {
        val listener = friendsRef.child(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friends = mutableListOf<Friend>()

                snapshot.children.forEach { friendSnapshot ->
                    val friend = friendSnapshot.getValue(Friend::class.java)
                    val friendId = friendSnapshot.key

                    if (friend != null && friendId != null) {
                        friends.add(friend.copy(id = friendId))
                    }
                }

                trySend(friends)
            }

            override fun onCancelled(error: DatabaseError) {
                close(Exception(error.message))
            }
        })

        awaitClose { friendsRef.child(currentUserId).removeEventListener(listener) }
    }

    // Additional utility methods
    fun isUserBlocked(userId: String): Flow<Boolean> = callbackFlow {
        val listener = blockedUsersRef.child(currentUserId)
            .orderByChild("blockedUserId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    close(Exception(error.message))
                }
            })

        awaitClose { blockedUsersRef.child(currentUserId).removeEventListener(listener) }
    }

    fun updateUserOnlineStatus(isOnline: Boolean) {
        if (currentUserId.isNotEmpty()) {
            val updates = hashMapOf<String, Any>(
                "users/$currentUserId/isOnline" to isOnline,
                "users/$currentUserId/lastSeen" to System.currentTimeMillis()
            )

            database.reference.updateChildren(updates)
        }
    }
}