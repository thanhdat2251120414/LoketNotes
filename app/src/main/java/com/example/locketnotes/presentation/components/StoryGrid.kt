@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.example.locketnotes.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.locketnotes.presentation.MyStoris.StoryViewModel
import com.example.locketnotes.presentation.domain.model.Story

@Composable
fun StoryGrid(
    modifier: Modifier = Modifier,
    viewModel: StoryViewModel
) {
    val stories by viewModel.stories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // State for edit dialog
    var showEditDialog by remember { mutableStateOf(false) }
    var editingStory by remember { mutableStateOf<Story?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && stories.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF667eea),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading your stories...",
                        color = Color(0xFF888888),
                        fontSize = 14.sp
                    )
                }
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "😔",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Something went wrong",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = error!!,
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            stories.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📝",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No stories yet",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "Start creating your first story\nand capture your memories!",
                        fontSize = 16.sp,
                        color = Color(0xFF888888),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stories.size) { index ->
                        val story = stories[index]
                        StoryCard(
                            story = story,
                            onEditClick = { selectedStory ->
                                editingStory = selectedStory
                                showEditDialog = true
                            },
                            onDeleteClick = { storyId ->
                                viewModel.deleteStory(storyId)
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && editingStory != null) {
        EditStoryDialog(
            story = editingStory!!,
            onDismiss = {
                showEditDialog = false
                editingStory = null
            },
            onSave = { updatedStory ->
                viewModel.updateStory(updatedStory)
                showEditDialog = false
                editingStory = null
            }
        )
    }
}

@Composable
fun EditStoryDialog(
    story: Story,
    onDismiss: () -> Unit,
    onSave: (Story) -> Unit
) {
    var editedMessage by remember { mutableStateOf(story.message) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Edit Story",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                // Date info (read-only)
                val date = remember(story.timestamp) {
                    java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(java.util.Date(story.timestamp))
                }
                Text(
                    text = "Created: $date",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )

                // Message input
                OutlinedTextField(
                    value = editedMessage,
                    onValueChange = { editedMessage = it },
                    label = { Text("Story Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea)
                    )
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF888888)
                        )
                    ) {
                        Text("Cancel")
                    }

                    // Save button
                    Button(
                        onClick = {
                            val updatedStory = story.copy(message = editedMessage.trim())
                            onSave(updatedStory)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea)
                        )
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}