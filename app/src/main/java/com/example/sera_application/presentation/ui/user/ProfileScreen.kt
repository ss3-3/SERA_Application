package com.example.sera_application.presentation.ui.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.presentation.ui.components.SafeProfileImageLoader
import com.example.sera_application.utils.bottomNavigationBar

data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    // Pass through actions that initiate navigation or require inputs
    onEditUserName: () -> Unit = {},
    onPasswordUpdate: () -> Unit = {},
    onOrderHistory: () -> Unit = {},
    onPaymentHistory: () -> Unit = {},
    onPaymentHistoryOrganizer: () -> Unit = {},
    onReservationManagement: () -> Unit = {},
    onReport: () -> Unit = {},
    onUserManagement: () -> Unit = {},
    onEventApproval: () -> Unit = {},
    onAdminReports: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    navController: androidx.navigation.NavController? = null
) {
    val user by viewModel.user.collectAsState()
    val currentUser = user
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val isAccountDeleted by viewModel.isAccountDeleted.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    // Refresh user data when entering screen
    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            onLogoutSuccess()
        }
    }

    LaunchedEffect(isAccountDeleted) {
        if (isAccountDeleted) {
            showDeleteDialog = false
            onDeleteAccount()
        }
    }

    val menuItems = if (currentUser != null) {
        getMenuItemsForRole(
            userRole = currentUser.role,
            onEditUserName = onEditUserName,
            onPasswordUpdate = onPasswordUpdate,
            onOrderHistory = onOrderHistory,
            onPaymentHistory = onPaymentHistory,
            onPaymentHistoryOrganizer = onPaymentHistoryOrganizer,
            onReservationManagement = onReservationManagement,
            onReport = onReport,
            onUserManagement = onUserManagement,
            onEventApproval = onEventApproval,
            onAdminReports = onAdminReports,
            onLogout = {
                showLogoutConfirmDialog = true
            },
            onDeleteAccount = { showDeleteDialog = true }
        )
    } else {
        MenuItemsGroup(emptyList(), emptyList())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Your Profile",
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
        },
        bottomBar = {
            navController?.let { nav ->
                bottomNavigationBar(
                    navController = nav,
                    currentRoute = nav.currentBackStackEntry?.destination?.route,
                    userRole = currentUser?.role
                )
            }
        },
        snackbarHost = {
            if (error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error ?: "")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        } else if (currentUser != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Profile Picture Section
                ProfilePictureSection(
                    userName = currentUser.fullName,
                    profileImageUrl = currentUser.profileImagePath,
                    onImageClick = {
                        imagePickerLauncher.launch("image/*")
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Menu Items Group 1
                MenuItemsCard(
                    items = menuItems.firstGroup,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Menu Items Group 2
                if (menuItems.secondGroup.isNotEmpty()) {
                    MenuItemsCard(
                        items = menuItems.secondGroup,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteDialog = false 
                        deleteConfirmationText = ""
                    },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
                    title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("This action is permanent and cannot be undone. All your data including reservations and profile info will be deleted.")
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Please type \"DELETE\" to confirm:", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = deleteConfirmationText,
                                onValueChange = { deleteConfirmationText = it },
                                placeholder = { Text("DELETE") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Red,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (deleteConfirmationText == "DELETE") {
                                    viewModel.deleteAccount()
                                    showDeleteDialog = false
                                    deleteConfirmationText = ""
                                }
                            },
                            enabled = deleteConfirmationText == "DELETE",
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Delete My Account", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showDeleteDialog = false 
                            deleteConfirmationText = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            // Logout Confirmation Dialog
            if (showLogoutConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutConfirmDialog = false },
                    title = {
                        Text(
                            text = "Confirm Logout",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to log out?",
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.logout()
                                showLogoutConfirmDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Logout")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutConfirmDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        } else {
             // Error or empty state
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                 Text("Failed to load profile. Please try again.")
             }
             // Could trigger navigation back to login
        }
    }
}

@Composable
private fun ProfilePictureSection(
    userName: String,
    profileImageUrl: String?,
    onImageClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture - Clickable to edit
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onImageClick),
            contentAlignment = Alignment.Center
        ) {
            SafeProfileImageLoader(
                imagePath = profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = userName,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

@Composable
private fun MenuItemsCard(
    items: List<ProfileMenuItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                MenuItemRow(
                    item = item
                )
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    item: ProfileMenuItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = Color(0xFF757575),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(20.dp)
        )
    }
}

data class MenuItemsGroup(
    val firstGroup: List<ProfileMenuItem>,
    val secondGroup: List<ProfileMenuItem>
)

@Composable
private fun getMenuItemsForRole(
    userRole: UserRole,
    onEditUserName: () -> Unit,
    onPasswordUpdate: () -> Unit,
    onOrderHistory: () -> Unit,
    onPaymentHistory: () -> Unit,
    onPaymentHistoryOrganizer: () -> Unit,
    onReservationManagement: () -> Unit,
    onReport: () -> Unit,
    onUserManagement: () -> Unit,
    onEventApproval: () -> Unit,
    onAdminReports: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
): MenuItemsGroup {
    return when (userRole) {
        UserRole.PARTICIPANT -> MenuItemsGroup(
            firstGroup = listOf(
                ProfileMenuItem("Edit User Name", Icons.Default.Edit, onEditUserName),
                ProfileMenuItem("Password Update", Icons.Default.Lock, onPasswordUpdate),
                ProfileMenuItem("Order History", Icons.Default.History, onOrderHistory),
                ProfileMenuItem("Payment History", Icons.Default.Payment, onPaymentHistory)
            ),
            secondGroup = listOf(
                ProfileMenuItem("Log out account", Icons.Default.ExitToApp, onLogout),
                ProfileMenuItem("Delete Account", Icons.Default.Delete, onDeleteAccount)
            )
        )
        UserRole.ORGANIZER -> MenuItemsGroup(
            firstGroup = listOf(
                ProfileMenuItem("Edit User Name", Icons.Default.Edit, onEditUserName),
                ProfileMenuItem("Password Update", Icons.Default.Lock, onPasswordUpdate),
                ProfileMenuItem("Reservation Management", Icons.Default.Event, onReservationManagement),
                ProfileMenuItem("Payment Management", Icons.Default.Payment, onPaymentHistoryOrganizer),
                ProfileMenuItem("Report", Icons.Default.Assessment, onReport)
            ),
            secondGroup = listOf(
                ProfileMenuItem("Log out account", Icons.Default.ExitToApp, onLogout)
            )
        )
        UserRole.ADMIN -> MenuItemsGroup(
            firstGroup = listOf(
                ProfileMenuItem("Edit User Name", Icons.Default.Edit, onEditUserName),
                ProfileMenuItem("Password Update", Icons.Default.Lock, onPasswordUpdate),
                ProfileMenuItem("User Management", Icons.Default.People, onUserManagement),
                ProfileMenuItem("Event Approval", Icons.Default.CheckCircle, onEventApproval),
                ProfileMenuItem("Report", Icons.Default.Assessment, onAdminReports)
            ),
            secondGroup = listOf(
                ProfileMenuItem("Log out account", Icons.Default.ExitToApp, onLogout)
            )
        )
    }
}
