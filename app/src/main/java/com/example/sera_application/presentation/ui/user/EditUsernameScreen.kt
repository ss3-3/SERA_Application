package com.example.sera_application.presentation.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUsernameScreen(
    currentUsername: String = "",
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onConfirm: (String) -> Unit = {}
) {
    var newUsername by remember { mutableStateOf(TextFieldValue(currentUsername)) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Username",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2C2C2E)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Prompt text
            Text(
                text = "Please enter your new username.",
                fontSize = 16.sp,
                color = Color(0xFF757575),
                modifier = Modifier.padding(top = 8.dp)
            )

            // New Username field
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "New Username",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Confirm button
                Button(
                    onClick = {
                        if (newUsername.text.isNotBlank()) {
                            showConfirmationDialog = true
                        }
                    },
                    modifier = Modifier
                        .width(120.dp)
                        .height(55.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    ),
                    enabled = newUsername.text.isNotBlank() && newUsername.text != currentUsername
                ) {
                    Text(
                        text = "Confirm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = "Confirm Username Change",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to change your username to \"${newUsername.text}\"?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(newUsername.text)
                        showConfirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditUsernameScreenPreview() {
    MaterialTheme {
        EditUsernameScreen(
            currentUsername = "THA_DCS2S1G9"
        )
    }
}

