package com.example.sera_application.presentation.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.sera_application.presentation.ui.components.SafeProfileImageLoader
import com.example.sera_application.presentation.viewmodel.user.AdminUserManagementViewModel
import com.example.sera_application.presentation.viewmodel.user.UserFilterType
import com.example.sera_application.utils.InputValidator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    navController: NavController,
    viewModel: AdminUserManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredUsers = remember(uiState.filterType, uiState.allUsers, uiState.searchQuery) {
        viewModel.getFilteredUsers()
    }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showUserDetailSheet by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

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
            // Search Bar
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { 
                        viewModel.updateSearchQuery(it)
                        // Validate search query
                        val (isValid, error) = InputValidator.validateSearchQuery(it)
                        searchError = error
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search by name, email, or user ID",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                viewModel.updateSearchQuery("")
                                searchError = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (searchError != null) Color(0xFFE91E63) else Color(0xFF2196F3),
                        unfocusedBorderColor = if (searchError != null) Color(0xFFE91E63) else Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        errorBorderColor = Color(0xFFE91E63)
                    ),
                    isError = searchError != null,
                    supportingText = searchError?.let { 
                        { Text(text = it, color = Color(0xFFE91E63), fontSize = 12.sp) }
                    },
                    singleLine = true
                )
                
                // Display error message below field if present
                if (searchError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = searchError!!,
                        color = Color(0xFFE91E63),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Section
                Text(
                    text = "Filter By Type",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FilterChip(
                            label = "All",
                            isSelected = uiState.filterType == UserFilterType.ALL,
                            onClick = { viewModel.updateFilter(UserFilterType.ALL) },
                            color = Color(0xFF2196F3)
                        )
                    }
                    item {
                        FilterChip(
                            label = "Participant",
                            isSelected = uiState.filterType == UserFilterType.PARTICIPANT,
                            onClick = { viewModel.updateFilter(UserFilterType.PARTICIPANT) },
                            color = Color(0xFF4CAF50)
                        )
                    }
                    item {
                        FilterChip(
                            label = "Approved Organizer",
                            isSelected = uiState.filterType == UserFilterType.APPROVED_ORGANIZER,
                            onClick = { viewModel.updateFilter(UserFilterType.APPROVED_ORGANIZER) },
                            color = Color(0xFF2196F3)
                        )
                    }
                    item {
                        FilterChip(
                            label = "Pending Organizer",
                            isSelected = uiState.filterType == UserFilterType.PENDING_ORGANIZER,
                            onClick = { viewModel.updateFilter(UserFilterType.PENDING_ORGANIZER) },
                            color = Color(0xFFFF9800)
                        )
                    }
                    item {
                        FilterChip(
                            label = "Suspended",
                            isSelected = uiState.filterType == UserFilterType.SUSPENDED,
                            onClick = { viewModel.updateFilter(UserFilterType.SUSPENDED) },
                            color = Color(0xFFE91E63)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                            Button(onClick = { viewModel.loadUsers() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                filteredUsers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Text(
                                text = when {
                                    uiState.searchQuery.isNotEmpty() -> "No users found matching your search"
                                    uiState.filterType == UserFilterType.PARTICIPANT -> "No participants found"
                                    uiState.filterType == UserFilterType.APPROVED_ORGANIZER -> "No approved organizers found"
                                    uiState.filterType == UserFilterType.PENDING_ORGANIZER -> "No pending organizers found"
                                    else -> "No users found"
                                },
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredUsers,
                            key = { it.userId }
                        ) { user ->
                            UserManagementCard(
                                user = user,
                                onViewClick = {
                                    selectedUser = user
                                    showUserDetailSheet = true
                                },
                                onActionClick = {
                                    selectedUser = user
                                    showUserDetailSheet = true
                                }
                            )
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
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
            onActivate = {
                viewModel.activateUser(selectedUser!!.userId)
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
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF5F5F5)
    val textColor = if (isSelected) color else Color.Gray

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun UserManagementCard(
    user: User,
    onViewClick: () -> Unit,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: User ID and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "User ID",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                
                // Status Badge
                StatusBadge(
                    status = if (!user.isApproved) "Pending"
                    else if (user.accountStatus == "SUSPENDED") "Suspended"
                    else "Active"
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            
            // User ID
            Text(
                text = user.userId,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // User Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                ) {
                    SafeProfileImageLoader(
                        imagePath = user.profileImagePath,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Role Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (user.role) {
                                com.example.sera_application.domain.model.enums.UserRole.PARTICIPANT -> Color(0xFFE3F2FD)
                                com.example.sera_application.domain.model.enums.UserRole.ORGANIZER -> Color(0xFFFFF3E0)
                                com.example.sera_application.domain.model.enums.UserRole.ADMIN -> Color(0xFFE8F5E9)
                            }
                        ) {
                            Text(
                                text = when (user.role) {
                                    com.example.sera_application.domain.model.enums.UserRole.PARTICIPANT -> "Participant"
                                    com.example.sera_application.domain.model.enums.UserRole.ORGANIZER -> "Organizer"
                                    com.example.sera_application.domain.model.enums.UserRole.ADMIN -> "Admin"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (user.role) {
                                    com.example.sera_application.domain.model.enums.UserRole.PARTICIPANT -> Color(0xFF2196F3)
                                    com.example.sera_application.domain.model.enums.UserRole.ORGANIZER -> Color(0xFFFF9800)
                                    com.example.sera_application.domain.model.enums.UserRole.ADMIN -> Color(0xFF4CAF50)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Member Since
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val memberSince = dateFormat.format(Date(user.createdAt))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Member Since",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Member since: $memberSince",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        !user.isApproved && user.role == com.example.sera_application.domain.model.enums.UserRole.ORGANIZER -> Color(0xFF2196F3)
                        user.accountStatus == "SUSPENDED" -> Color(0xFF4CAF50)
                        else -> Color(0xFFE91E63)
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when {
                        !user.isApproved && user.role == com.example.sera_application.domain.model.enums.UserRole.ORGANIZER -> "Approve Organizer"
                        user.accountStatus == "SUSPENDED" -> "Activate User"
                        else -> "Suspend User"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val backgroundColor = when (status.lowercase()) {
        "active" -> Color(0xFF4CAF50)
        "suspended" -> Color(0xFFE91E63)
        "pending" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
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
    onActivate: () -> Unit,
    onApprove: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPending = !user.isApproved
    val isSuspended = user.accountStatus == "SUSPENDED"
    val isOrganizer = user.role == com.example.sera_application.domain.model.enums.UserRole.ORGANIZER

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
            // Profile Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            ) {
                SafeProfileImageLoader(
                    imagePath = user.profileImagePath,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
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
                else if (user.accountStatus == "SUSPENDED") "Suspended"
                else "Active"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Details
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("User ID", user.userId)
                DetailRow(
                    "Role",
                    when (user.role) {
                        com.example.sera_application.domain.model.enums.UserRole.PARTICIPANT -> "Participant"
                        com.example.sera_application.domain.model.enums.UserRole.ORGANIZER -> "Organizer"
                        com.example.sera_application.domain.model.enums.UserRole.ADMIN -> "Admin"
                    }
                )
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val memberSince = dateFormat.format(Date(user.createdAt))
                DetailRow("Member Since", memberSince)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            if (isPending && isOrganizer) {
                // Approve button for pending organizers
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Approve Organizer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // Activate/Suspend button for other users
                Button(
                    onClick = if (isSuspended) onActivate else onSuspend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuspended) Color(0xFF4CAF50) else Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isSuspended) "Activate" else "Suspend",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
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

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Normal
        )
    }
}
