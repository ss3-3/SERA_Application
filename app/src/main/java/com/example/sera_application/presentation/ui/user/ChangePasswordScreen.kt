package com.example.sera_application.presentation.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onConfirm: (String, String, String) -> Unit = {olePassword, newPassword, confirmPassword -> } // oldPassword, newPassword, confirmPassword
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var showConfirmationDialog by remember { mutableStateOf(false) }

    var passwordError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Change Password",
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
                text = "Please fill-in to change your password.",
                fontSize = 16.sp,
                color = Color(0xFF757575),
                modifier = Modifier.padding(top = 8.dp)
            )

            // Old Password field
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Old password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = {
                        oldPassword = it
                        passwordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOldPassword = !showOldPassword }) {
                            Icon(
                                imageVector = if (showOldPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showOldPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )
            }

            // New Password field
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "New password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showNewPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )
            }

            // Confirm Password field
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Confirm password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true,
                    isError = passwordError != null
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Confirm button
                    Button(
                        onClick = {
                            // Validate passwords
                            when {
                                oldPassword.isBlank() -> {
                                    passwordError = "Please enter your old password"
                                }

                                newPassword.isBlank() -> {
                                    passwordError = "Please enter a new password"
                                }

                                confirmPassword.isBlank() -> {
                                    passwordError = "Please confirm your new password"
                                }

                                newPassword != confirmPassword -> {
                                    passwordError = "New passwords do not match"
                                }

                                newPassword.length < 6 -> {
                                    passwordError = "Password must be at least 6 characters"
                                }

                                else -> {
                                    passwordError = null
                                    showConfirmationDialog = true
                                }
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
                        enabled = oldPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
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
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = "Confirm Password Change",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to change your password?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(oldPassword, newPassword, confirmPassword)
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
private fun ChangePasswordScreenPreview() {
    MaterialTheme {
        ChangePasswordScreen()
    }
}
