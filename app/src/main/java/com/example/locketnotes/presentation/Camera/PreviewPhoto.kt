package com.example.homeloketnotes.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.locketnotes.presentation.viewmodel.CameraViewModel


@Composable
fun PreviewPhoto(
    photoUri: String,
    viewModel: CameraViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val errorMessage by viewModel.errorMessage.collectAsState()
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    var text by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
            isUploading = false
        }
    }

    LaunchedEffect(uploadStatus) {
        uploadStatus?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUploadStatus()
            isUploading = false
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(35.dp))

            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Send to...",
                    fontSize = 24.sp,
                    color = Color.Black
                )
            }

            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Save Photo",
                tint = Color.Black,
                modifier = Modifier
                    .size(35.dp)
                    .clickable {
                        viewModel.saveCapturedPhoto()
                        Toast.makeText(context, "Photo saved to gallery!", Toast.LENGTH_SHORT).show()
                    }
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Captured Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Add message") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        viewModel.discardPhoto()
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Discard",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (isUploading) Color.Gray else Color.Black)
                    .clickable(enabled = !isUploading) {
                        if (!isUploading) {
                            showDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    Text("...", color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Xác nhận gửi ảnh") },
            text = { Text("Bạn muốn đăng công khai hay lưu riêng tư?") },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            isUploading = true
                            showDialog = false
                            viewModel.uploadPhotoToFirebase(text, isPublic = false)
                            Toast.makeText(context, "Lưu ảnh riêng tư...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Riêng tư")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isUploading = true
                            showDialog = false
                            viewModel.uploadPhotoToFirebase(text, isPublic = true)
                            Toast.makeText(context, "Đang tải ảnh công khai...", Toast.LENGTH_SHORT).show()
                        },

                        ) {
                        Text(
                            text = "Công khai"
                        )
                    }
                }
            },
            dismissButton = {}
        )
    }
}