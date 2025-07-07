package com.example.locketnotes.presentation.Camera

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.compose.foundation.clickable
import com.example.locketnotes.presentation.domain.model.UserData


@Composable
fun SettingsScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current

    var username by remember { mutableStateOf("Personal Profile") }
    var email by remember { mutableStateOf("Manage your info") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // Fetch user data
    LaunchedEffect(userId) {
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().reference
            val userRef = database.child("user").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(UserData::class.java)
                    userData?.let {
                        username = it.username.ifBlank { "Personal Profile" }
                        email = it.email.ifBlank { "Manage your info" }
                        profileImageUrl = it.profileImageUrl
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profileImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val settings = listOf(
            Triple("My Home", Icons.Default.Home, "camera"),
            Triple("Messages", Icons.Default.Email, "messages"),
            Triple("My Profile", Icons.Default.AccountBox, "myprofile"),
            Triple("Sign Out", Icons.Default.ExitToApp, "login")
        )

        settings.forEach { (label, icon, route) ->
            SettingItem(label = label, icon = icon) {
                when (route) {
                    "camera" -> navController.navigate("camera")
                    "login" -> navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                    "myprofile" -> navController.navigate("myprofile")
                    else -> navController.navigate(route)
                }
            }
        }
    }
}

@Composable
fun SettingItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val isSignOut = label.equals("Sign Out", ignoreCase = true)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable{ onClick() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSignOut) Color.Red else MaterialTheme.colorScheme.onBackground
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.3f))
    }
}
