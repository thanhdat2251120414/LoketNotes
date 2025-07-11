package com.example.locketnotes.presentation.Camera

import android.app.Activity
import android.content.Intent
import android.media.MediaActionSound
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.homeloketnotes.presentation.PreviewPhoto
import com.example.locketnotes.MainActivity
import com.example.locketnotes.R
import com.example.locketnotes.presentation.components.BottomNavBar
import com.example.locketnotes.presentation.components.TopBar
import com.example.locketnotes.presentation.viewmodel.CameraViewModel

@Composable
fun CameraScreen(
    activity: Activity,
    navController: NavController
) {
    val controller = remember {
        LifecycleCameraController(activity.applicationContext).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    val cameraSound = remember {
        MediaActionSound().apply {
            load(MediaActionSound.SHUTTER_CLICK)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraSound.release()
        }
    }

    val cameraViewModel = hiltViewModel<CameraViewModel>()
    val showPreview by cameraViewModel.showPreview.collectAsState()
    val capturedPhotoUri by cameraViewModel.capturedPhotoUri.collectAsState()

    if (showPreview && capturedPhotoUri != null) {
        PreviewPhoto(
            photoUri = capturedPhotoUri!!,
            viewModel = cameraViewModel,
            onBack = {}
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // ✅ Gọi lại TopBar
                TopBar(centerText = "Loket Camera", navController = navController)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.LightGray)
                            .clickable {
                                navController.navigate("friends")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Friend",
                            fontSize = 20.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                // Camera View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(Color.White),
                ) {
                    val lifecycleOwner = LocalLifecycleOwner.current

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentHeight()
                    ) {
                        AndroidView(
                            modifier = Modifier
                                .width(400.dp)
                                .height(400.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            factory = { context ->
                                PreviewView(context).apply {
                                    this.controller = controller
                                    controller.bindToLifecycle(lifecycleOwner)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Open Gallery
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(Color.White)
                                    .clickable {
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("content://media/internal/images/media")
                                        ).also {
                                            activity.startActivity(it)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = stringResource(R.string.open_gallery),
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(1.dp))

                            // Take Photo
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(60.dp)
                                    .background(Color.Black)
                                    .clickable {
                                        if ((activity as MainActivity).arePermissionsGranted()) {
                                            cameraSound.play(MediaActionSound.SHUTTER_CLICK)
                                            cameraViewModel.onTakePhoto(controller)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = stringResource(R.string.take_photo),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(1.dp))

                            // Switch Camera
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(Color.White)
                                    .clickable {
                                        controller.cameraSelector =
                                            if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                                CameraSelector.DEFAULT_FRONT_CAMERA
                                            } else {
                                                CameraSelector.DEFAULT_BACK_CAMERA
                                            }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cameraswitch,
                                    contentDescription = stringResource(R.string.switch_camera_preview),
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                // ✅ Bottom Navigation Bar
                BottomNavBar(navController = navController)
            }
        }
    }
}
