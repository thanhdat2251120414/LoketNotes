package com.example.locketnotes.presentation.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.locketnotes.presentation.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _chatPartner = MutableStateFlow<UserData?>(null)
    val chatPartner = _chatPartner.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    private var messagesListener: ValueEventListener? = null
    private var typingListener: ValueEventListener? = null

    fun initChat(partnerId: String) {
        _isLoading.value = true
        val currentUserId = auth.currentUser?.uid ?: return
        loadChatPartner(partnerId)

        database.reference.child("user").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserData::class.java)
                    user?.let {
                        _currentUser.value = it.copy(userId = currentUserId)
                        loadMessages(partnerId)
                        listenToTypingStatus(partnerId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Failed to load current user: ${error.message}"
                }
            })
    }

    private fun loadChatPartner(partnerId: String) {
        database.reference.child("user").child(partnerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val partner = snapshot.getValue(UserData::class.java)
                    partner?.let {
                        _chatPartner.value = it.copy(userId = partnerId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Failed to load chat partner: ${error.message}"
                }
            })
    }

    private fun loadMessages(partnerId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = getChatId(currentUserId, partnerId)

        messagesListener = database.reference.child("chats").child(chatId).child("messages")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Message>()
                    snapshot.children.forEach {
                        val msg = it.getValue(Message::class.java)
                        msg?.let { list.add(it) }
                    }
                    _messages.value = list
                    _isLoading.value = false

                    markMessagesAsRead(chatId, currentUserId)
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Failed to load messages: ${error.message}"
                    _isLoading.value = false
                }
            })
    }

    private fun markMessagesAsRead(chatId: String, currentUserId: String) {
        _messages.value.filter {
            it.receiverId == currentUserId && !it.isRead
        }.forEach { message ->
            database.reference.child("chats").child(chatId)
                .child("messages").child(message.id)
                .child("isRead").setValue(true)
        }
    }

    fun sendMessage(
        content: String,
        partnerId: String,
        messageType: MessageType = MessageType.TEXT
    ) {
        if (content.trim().isEmpty() && messageType == MessageType.TEXT) return
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = getChatId(currentUserId, partnerId)
        val messageId = database.reference.child("chats").child(chatId).child("messages").push().key ?: return

        val message = Message(
            id = messageId,
            senderId = currentUserId,
            receiverId = partnerId,
            content = content.trim(),
            timestamp = System.currentTimeMillis(),
            messageType = messageType
        )

        database.reference.child("chats").child(chatId).child("messages").child(messageId)
            .setValue(message)
            .addOnSuccessListener {
                updateChatInfo(chatId, currentUserId, partnerId, content)
            }
            .addOnFailureListener {
                _error.value = "Failed to send message: ${it.message}"
            }
    }

    fun sendImageMessage(imageUri: Uri, partnerId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = getChatId(currentUserId, partnerId)
        val messageId = database.reference.child("chats").child(chatId).child("messages").push().key ?: return
        val storageRef = storage.reference.child("chat_images/${chatId}_$messageId")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageMessage = Message(
                        id = messageId,
                        senderId = currentUserId,
                        receiverId = partnerId,
                        content = "Image",
                        timestamp = System.currentTimeMillis(),
                        messageType = MessageType.IMAGE,
                        fileUrl = uri.toString()
                    )
                    database.reference.child("chats").child(chatId).child("messages").child(messageId)
                        .setValue(imageMessage)
                        .addOnSuccessListener {
                            updateChatInfo(chatId, currentUserId, partnerId, "Image")
                        }
                }
            }
            .addOnFailureListener {
                _error.value = "Failed to send image: ${it.message}"
            }
    }

    private fun updateChatInfo(chatId: String, currentUserId: String, partnerId: String, lastMessage: String) {
        val chatInfo = ChatInfo(
            chatId = chatId,
            participants = listOf(currentUserId, partnerId),
            lastMessage = lastMessage,
            lastMessageTime = System.currentTimeMillis(),
            lastMessageSender = currentUserId
        )
        database.reference.child("chats").child(chatId).child("info").setValue(chatInfo)
    }

    fun setTypingStatus(partnerId: String, isTyping: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = getChatId(currentUserId, partnerId)
        database.reference.child("chats").child(chatId).child("typing").child(currentUserId)
            .setValue(if (isTyping) System.currentTimeMillis() else null)
    }

    private fun listenToTypingStatus(partnerId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = getChatId(currentUserId, partnerId)
        typingListener = database.reference.child("chats").child(chatId).child("typing").child(partnerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lastTyping = snapshot.getValue(Long::class.java)
                    _isTyping.value = lastTyping != null && (System.currentTimeMillis() - lastTyping) < 5000
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_$userId2" else "${userId2}_$userId1"
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.let {
            database.reference.removeEventListener(it)
        }
        typingListener?.let {
            database.reference.removeEventListener(it)
        }
    }
}
