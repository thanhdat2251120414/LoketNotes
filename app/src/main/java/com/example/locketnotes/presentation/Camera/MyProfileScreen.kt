package com.example.locketnotes.presentation.Camera

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.locketnotes.R
import com.example.locketnotes.presentation.domain.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*

@Composable
fun MyProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedGender by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "None")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { selectedImageUri = it.toString() } }

    LaunchedEffect(userId) {
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().reference
            val userRef = database.child("user").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(UserData::class.java)
                    userData?.let {
                        username = it.username
                        email = it.email
                        gender = it.gender ?: ""
                        dob = it.birthDate?.let { millis ->
                            java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(millis))
                        } ?: ""
                        selectedImageUri = it.profileImageUrl
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Edit profile",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Avatar
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.size(120.dp)
        ) {
            if (!selectedImageUri.isNullOrBlank()) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    tint = Color.Gray
                )
            }

            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "Change Avatar",
                    tint = Color.Unspecified
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        EditableProfileField(label = "User name", value = username) { username = it }
        ReadOnlyProfileField(label = "Email", value = email)

        GenderDropdownField(
            label = "Gender",
            selectedGender = gender,
            options = genderOptions,
            expanded = expandedGender,
            onExpandedChange = { expandedGender = it },
            onGenderSelected = { gender = it }
        )

        DatePickerField(
            label = "Date of birth",
            selectedDate = dob,
            showDatePicker = showDatePicker,
            onShowDatePickerChange = { showDatePicker = it },
            onDateSelected = { dob = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (userId == null) {
                    Toast.makeText(context, "Không tìm thấy user!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                updateUserProfile(
                    context = context,
                    coroutineScope = coroutineScope,
                    userId = userId,
                    username = username,
                    email = email,
                    gender = gender,
                    dob = dob,
                    selectedImageUri = selectedImageUri,
                    onSuccess = {
                        Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Lỗi: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}


