package com.example.locketnotes.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locketnotes.presentation.data.repository.FriendsRepository
import com.example.locketnotes.presentation.data.repository.FriendsRepositoryImpl
import com.example.locketnotes.presentation.domain.model.Friend
import com.example.locketnotes.presentation.domain.model.FriendRequest
import com.example.locketnotes.presentation.domain.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUiState(
    val isLoading: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val searchResults: List<UserData> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null
)

class FriendsViewModel(
    private val repository: FriendsRepository = FriendsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    // 👉 Thêm danh sách ID bạn bè đã xác nhận
    private val _friendIds = MutableStateFlow<Set<String>>(emptySet())
    val friendIds: StateFlow<Set<String>> = _friendIds.asStateFlow()

    init {
        loadFriends()
        loadFriendRequests()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getFriends().fold(
                onSuccess = { friends: List<Friend> ->
                    // Lưu lại danh sách friendId
                    val ids = friends.mapNotNull { it.user.userId }.toSet()
                    _friendIds.value = ids

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        friends = friends,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun loadFriendRequests() {
        viewModelScope.launch {
            repository.getFriendRequests().fold(
                onSuccess = { requests ->
                    _uiState.value = _uiState.value.copy(friendRequests = requests)
                },
                onFailure = { error ->
                    showToast("Không thể tải lời mời kết bạn: ${error.message}")
                }
            )
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)

            repository.searchUsers(query).fold(
                onSuccess = { users ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = users
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            repository.sendFriendRequest(userId).fold(
                onSuccess = {
                    showToast("Đã gửi lời mời kết bạn")

                    // Cập nhật trạng thái PENDING cho user tương ứng trong searchResults
                    _uiState.value = _uiState.value.copy(
                        searchResults = _uiState.value.searchResults.map { user ->
                            if (user.userId == userId) {
                                user.copy(requestStatus = com.example.locketnotes.presentation.domain.model.RequestStatus.PENDING)
                            } else user
                        }
                    )
                },
                onFailure = { error ->
                    showToast("Không thể gửi lời mời: ${error.message}")
                }
            )
        }
    }


    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(requestId).fold(
                onSuccess = {
                    showToast("Đã chấp nhận lời mời kết bạn")
                    loadFriends()
                    loadFriendRequests()
                },
                onFailure = { error -> showToast("Không thể chấp nhận: ${error.message}") }
            )
        }
    }

    fun declineFriendRequest(requestId: String) {
        viewModelScope.launch {
            repository.declineFriendRequest(requestId).fold(
                onSuccess = {
                    showToast("Đã từ chối lời mời kết bạn")
                    loadFriendRequests()
                },
                onFailure = { error -> showToast("Không thể từ chối: ${error.message}") }
            )
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            repository.removeFriend(friendId).fold(
                onSuccess = {
                    showToast("Đã xóa bạn bè")
                    loadFriends()

                    // ✅ Gọi lại search nếu đang có kết quả tìm kiếm
                    val currentQuery = _uiState.value.searchResults.firstOrNull()?.email ?: ""
                    if (currentQuery.isNotBlank()) {
                        searchUsers(currentQuery)
                    }
                },
                onFailure = { error -> showToast("Không thể xóa bạn: ${error.message}") }
            )
        }
    }


    fun blockUser(userId: String) {
        viewModelScope.launch {
            repository.blockUser(userId).fold(
                onSuccess = {
                    showToast("Đã chặn người dùng")
                    loadFriends()
                },
                onFailure = { error -> showToast("Không thể chặn: ${error.message}") }
            )
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    private fun showToast(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }

    fun isFriendWith(userId: String): Boolean {
        return friendIds.value.contains(userId)
    }
}