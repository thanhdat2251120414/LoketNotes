package com.example.locketnotes.presentation.screens

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homeloketnotes.data.repository.uploadToCloudinary
import com.example.locketnotes.presentation.MyStoris.StoryViewModel
import com.example.locketnotes.presentation.components.BottomNavBar
import com.example.locketnotes.presentation.components.StoryGrid
import com.example.locketnotes.presentation.components.TopBar
import com.example.locketnotes.presentation.domain.model.Story
import kotlinx.coroutines.launch

@Composable
fun MyStoriesScreen(
    navController: NavController,
    viewModel: StoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploading = true
            scope.launch {
                try {

                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }

                    uploadToCloudinary(
                        bitmap = bitmap,
                        onSuccess = { uploadedUrl ->
                            val newStory = Story(
                                id = "",
                                imageUrl = uploadedUrl,
                                message = "Tải ảnh lúc ${System.currentTimeMillis()}",
                                timestamp = System.currentTimeMillis(),
                                userId = "testUser"
                            )
                            viewModel.addStory(newStory)
                            uploading = false
                        },
                        onError = {
                            uploading = false
                            it.printStackTrace()
                        }
                    )
                } catch (e: Exception) {
                    uploading = false
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = { TopBar(centerText = "My story", navController = navController) },
        bottomBar = { BottomNavBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                imagePickerLauncher.launch("image/*")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add story")
            }
        },
        content = { padding ->
            StoryGrid(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                viewModel = viewModel
            )

            if (uploading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    )
}
