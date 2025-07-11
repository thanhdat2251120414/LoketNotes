package com.example.locketnotes.presentation.MyStoris

import com.example.locketnotes.presentation.domain.model.Story


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locketnotes.presentation.data.repository.StoryRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoryViewModel(
    private val repository: StoryRepository = StoryRepository()
) : ViewModel() {

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadStories()
    }

    private fun loadStories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllStories().collectLatest  { storiesList: List<Story> ->
                    _stories.value = storiesList
                    _isLoading.value = false
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun addStory(story: Story) {
        repository.addStory(story)
    }

    fun updateStory(story: Story) {
        repository.updateStory(story)
    }


    fun deleteStory(storyId: String) {
        repository.deleteStory(storyId)
    }

    fun clearError() {
        _error.value = null
    }

}