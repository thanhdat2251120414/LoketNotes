package com.example.homeloketnotes.presentation

import android.graphics.Bitmap
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeloketnotes.domain.repositry.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository
): ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _capturedPhotoUri = MutableStateFlow<String?>(null)
    val capturedPhotoUri = _capturedPhotoUri.asStateFlow()

    private val _showPreview = MutableStateFlow(false)
    val showPreview = _showPreview.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var pendingPhotoBitmap: Bitmap? = null

    fun onTakePhoto(controller: LifecycleCameraController) {
        viewModelScope.launch {
            try {
                cameraRepository.takePhoto(controller) { bitmap, tempUri ->
                    // Store the bitmap temporarily and show preview
                    pendingPhotoBitmap = bitmap
                    _capturedPhotoUri.value = tempUri
                    _showPreview.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to take photo: ${e.message}"
            }
        }
    }

    fun saveCapturedPhoto() {
        pendingPhotoBitmap?.let { bitmap ->
            viewModelScope.launch {
                try {
                    cameraRepository.savePhotoToPermanentStorage(bitmap)
                    _showPreview.value = false
                    _capturedPhotoUri.value = null
                    pendingPhotoBitmap = null
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to save photo: ${e.message}"
                }
            }
        }
    }

    fun discardPhoto() {
        pendingPhotoBitmap = null
        _capturedPhotoUri.value = null
        _showPreview.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        pendingPhotoBitmap?.recycle()
    }

    fun onRecordVideo(
        controller: LifecycleCameraController
    ){

        _isRecording.update { !isRecording.value }

        viewModelScope.launch {
            cameraRepository.recordVideo(controller)
        }
    }
}
