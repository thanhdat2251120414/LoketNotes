package com.example.locketnotes.presentation.Camera


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.homeloketnotes.data.repository.saveUserToDatabase
import com.example.homeloketnotes.data.repository.uploadToCloudinary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditableProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FFF0), shape = RoundedCornerShape(24.dp)),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ReadOnlyProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FFF0), shape = RoundedCornerShape(24.dp)),
            readOnly = true,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdownField(
    label: String,
    selectedGender: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onGenderSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedGender,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .background(Color(0xFFF0FFF0), shape = RoundedCornerShape(24.dp)),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onGenderSelected(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: String,
    showDatePicker: Boolean,
    onShowDatePickerChange: (Boolean) -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FFF0), shape = RoundedCornerShape(24.dp))
                .clickable { onShowDatePickerChange(true) },
            trailingIcon = {
                IconButton(onClick = { onShowDatePickerChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }

    if (showDatePicker) {
        CustomDatePickerDialog(
            datePickerState = datePickerState,
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let {
                    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val date = java.util.Date(it)
                    onDateSelected(formatter.format(date))
                }
                onShowDatePickerChange(false)
            },
            onDismiss = { onShowDatePickerChange(false) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    datePickerState: DatePickerState,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
fun updateUserProfile(
    context: android.content.Context,
    coroutineScope: CoroutineScope,
    userId: String,
    username: String,
    email: String,
    gender: String,
    dob: String,
    selectedImageUri: String?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    coroutineScope.launch {
        var finalProfileUrl = selectedImageUri

        if (selectedImageUri != null && selectedImageUri.startsWith("content://")) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    android.provider.MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        android.net.Uri.parse(selectedImageUri)
                    )
                }

                uploadToCloudinary(
                    bitmap = bitmap,
                    onSuccess = { uploadedUrl -> finalProfileUrl = uploadedUrl },
                    onError = { e ->
                        onError("Upload ảnh lỗi: ${e.message}")
                        return@uploadToCloudinary
                    }
                )
            } catch (e: Exception) {
                onError("Lỗi xử lý ảnh: ${e.message}")
                return@launch
            }
        }

        saveUserToDatabase(
            userId = userId,
            username = username,
            email = email,
            gender = gender.takeIf { it.isNotBlank() },
            birthDate = dob.toMillisOrNull(),
            profileImageUrl = finalProfileUrl,
            onSuccess = { onSuccess() },
            onError = { err -> onError(err) }
        )
    }
}

private fun String.toMillisOrNull(): Long? {
    return try {
        java.text.SimpleDateFormat("yyyy-MM-dd").parse(this)?.time
    } catch (e: Exception) {
        null
    }
}

