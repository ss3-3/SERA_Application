package com.example.sera_application.presentation.ui.user

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.presentation.viewmodel.UserViewModel

data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    // Pass through actions that initiate navigation or require inputs
    onEditUserName: () -> Unit = {},
    onPasswordUpdate: () -> Unit = {},
    onOrderHistory: () -> Unit = {},
    onPaymentHistory: () -> Unit = {},
    onReservationManagement: () -> Unit = {},
    onReport: () -> Unit = {},
    onUserManagement: () -> Unit = {},
    onEventApproval: () -> Unit = {},
    onAdminReports: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser
    
    // Refresh user data when entering screen
    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    val menuItems = if (currentUser != null) {
        getMenuItemsForRole(
            userRole = currentUser.role,
            onEditUserName = onEditUserName,
            onPasswordUpdate = onPasswordUpdate,
            onOrderHistory = onOrderHistory,
            onPaymentHistory = onPaymentHistory,
            onReservationManagement = onReservationManagement,
            onReport = onReport,
            onUserManagement = onUserManagement,
            onEventApproval = onEventApproval,
            onAdminReports = onAdminReports,
            onLogout = {
                viewModel.logout { onLogoutSuccess() }
            },
            onDeleteAccount = onDeleteAccount
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
            if (currentUser != null) {
                BottomNavigationBar(
                    userRole = currentUser.role,
                    onHomeClick = onHomeClick,
                    onAddEventClick = onAddEventClick,
                    onProfileClick = onProfileClick
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
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
                    profileImageUrl = currentUser.profileImagePath
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
        } else {
             // Error or empty state
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                 Text("Failed to load profile. Please try again.")
             }
             // Could trigger navigation back to login
        }
    }
}

// ... Rest of the file (ProfilePictureSection, MenuItemsCard, etc.) remains mostly the same 
// but I'll need to append them if write_to_file overwrites everything.
// Since write_to_file DOES overwrite, I must include the helper components.

@Composable
private fun ProfilePictureSection(
    userName: String,
    profileImageUrl: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUrl != null) {
                // TODO: Load image from URL using Coil
                 Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF757575)
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF757575)
                )
            }
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

@Composable
private fun BottomNavigationBar(
    userRole: UserRole,
    onHomeClick: () -> Unit,
    onAddEventClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        when (userRole) {
            UserRole.PARTICIPANT -> {
                // Participant: Home | Me
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = false,
                        onClick = onHomeClick
                    )
                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Me",
                        isSelected = true,
                        onClick = onProfileClick
                    )
                }
            }

            UserRole.ORGANIZER -> {
                // Organizer: Home | Add Event (+) | Me
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = false,
                        onClick = onHomeClick
                    )
                    FloatingActionButton(
                        onClick = onAddEventClick,
                        modifier = Modifier.size(56.dp),
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Event",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Me",
                        isSelected = true,
                        onClick = onProfileClick
                    )
                }
            }

            UserRole.ADMIN -> {
                // Admin: Home | Event | Registration | Me
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = false,
                        onClick = onHomeClick
                    )
                    BottomNavItem(
                        icon = Icons.Default.Event,
                        label = "Event",
                        isSelected = false,
                        onClick = onAddEventClick
                    )
                    BottomNavItem(
                        icon = Icons.Default.Assignment,
                        label = "Registration",
                        isSelected = false,
                        onClick = {}
                    )
                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Me",
                        isSelected = true,
                        onClick = onProfileClick
                    )
                }
            }
        }
    }
}
@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF1976D2) else Color(0xFF757575),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF1976D2) else Color(0xFF757575)
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
