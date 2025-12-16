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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sera_application.domain.model.enums.UserRole

data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userRole: UserRole,
    profileImageUrl: String? = null,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEditUserName: () -> Unit = {},
    onPasswordUpdate: () -> Unit = {},
    onOrderHistory: () -> Unit = {},
    onPaymentHistory: () -> Unit = {},
    onReservationManagement: () -> Unit = {},
    onReport: () -> Unit = {},
    onUserManagement: () -> Unit = {},
    onEventApproval: () -> Unit = {},
    onAdminReports: () -> Unit = {},
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val menuItems = getMenuItemsForRole(
        userRole = userRole,
        onEditUserName = onEditUserName,
        onPasswordUpdate = onPasswordUpdate,
        onOrderHistory = onOrderHistory,
        onPaymentHistory = onPaymentHistory,
        onReservationManagement = onReservationManagement,
        onReport = onReport,
        onUserManagement = onUserManagement,
        onEventApproval = onEventApproval,
        onAdminReports = onAdminReports,
        onLogout = onLogout,
        onDeleteAccount = onDeleteAccount
    )

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
            BottomNavigationBar(
                userRole = userRole,
                onHomeClick = onHomeClick,
                onAddEventClick = onAddEventClick,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture Section
            ProfilePictureSection(
                userName = userName,
                profileImageUrl = profileImageUrl
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
    }
}

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
                // AsyncImage(...)
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
                    item = item,
                    showDivider = index < items.size - 1
                )
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    item: ProfileMenuItem,
    showDivider: Boolean = true
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
    if (showDivider) {
        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = when (userRole) {
                UserRole.PARTICIPANT -> Arrangement.SpaceEvenly
                UserRole.ORGANIZER -> Arrangement.SpaceEvenly
                UserRole.ADMIN -> Arrangement.SpaceEvenly
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Button (All Roles)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onHomeClick)
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Home",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            // Add Event Button (Organizer Only)
            if (userRole == UserRole.ORGANIZER) {
                FloatingActionButton(
                    onClick = onAddEventClick,
                    modifier = Modifier.size(56.dp),
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Event",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(56.dp))
            }

            // Profile Button (All Roles)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onProfileClick)
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Me",
                    tint = Color(0xFF1976D2), // Highlighted when selected
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Me",
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2) // Highlighted when selected
                )
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen(
            userName = "Participant1",
            userRole = UserRole.PARTICIPANT
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenOrganizerPreview() {
    MaterialTheme {
        ProfileScreen(
            userName = "Organizer1",
            userRole = UserRole.ORGANIZER
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenAdminPreview() {
    MaterialTheme {
        ProfileScreen(
            userName = "Admin1",
            userRole = UserRole.ADMIN
        )
    }
}
