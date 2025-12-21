package com.example.sera_application.presentation.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sera_application.domain.model.User
import com.example.sera_application.presentation.viewmodel.user.AdminUserManagementViewModel
import com.example.sera_application.presentation.viewmodel.user.UserFilterType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    navController: NavController,
    viewModel: AdminUserManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredUsers = remember(uiState.filterType, uiState.allUsers) {
        viewModel.getFilteredUsers()
    }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showUserDetailSheet by remember { mutableStateOf(false) }

    // Load users on first composition
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "User Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterPill(
                    label = "All",
                    isSelected = uiState.filterType == UserFilterType.ALL,
                    onClick = { viewModel.updateFilter(UserFilterType.ALL) }
                )
                FilterPill(
                    label = "Pending Organizer",
                    isSelected = uiState.filterType == UserFilterType.PENDING_ORGANIZER,
                    onClick = { viewModel.updateFilter(UserFilterType.PENDING_ORGANIZER) }
                )
            }

            // User List
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                filteredUsers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.filterType == UserFilterType.PENDING_ORGANIZER)
                                "No pending organizers"
                            else
                                "No organizers found",
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredUsers) { user ->
                            UserManagementCard(
                                user = user,
                                onViewClick = {
                                    selectedUser = user
                                    showUserDetailSheet = true
                                },
                                onRemoveClick = {
                                    viewModel.removeUser(user.userId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // User Detail Bottom Sheet
    if (showUserDetailSheet && selectedUser != null) {
        UserDetailBottomSheet(
            user = selectedUser!!,
            onDismiss = {
                showUserDetailSheet = false
                selectedUser = null
            },
            onSuspend = {
                viewModel.suspendUser(selectedUser!!.userId)
                showUserDetailSheet = false
                selectedUser = null
            },
            onApprove = {
                viewModel.approveOrganizer(selectedUser!!.userId)
                showUserDetailSheet = false
                selectedUser = null
            }
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

@Composable
private fun UserManagementCard(
    user: User,
    onViewClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Profile icon and user info
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Organizer",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Status Badge
                    StatusBadge(
                        status = if (!user.isApproved) "Pending"
                        else if (user.accountStatus == "SUSPENDED") "Suspend"
                        else "Active"
                    )
                }
            }

            // Right side: Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Button
                Button(
                    onClick = onViewClick,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "View",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                // Remove Icon
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val backgroundColor = when (status.lowercase()) {
        "active" -> Color(0xFF4CAF50)
        "suspend" -> Color(0xFFE91E63)
        "pending" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDetailBottomSheet(
    user: User,
    onDismiss: () -> Unit,
    onSuspend: () -> Unit,
    onApprove: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPending = !user.isApproved

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = user.fullName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge
            StatusBadge(
                status = if (isPending) "Pending"
                else if (user.accountStatus == "SUSPENDED") "Suspend"
                else "Active"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Member Since
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val memberSince = dateFormat.format(Date(user.createdAt))
            Text(
                text = "Member Since: $memberSince",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = if (isPending) onApprove else onSuspend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPending) Color(0xFF2196F3) else Color(0xFFE91E63)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isPending) "Approve" else "Suspend",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Close Button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Gray
                )
            ) {
                Text(
                    text = "Close",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
