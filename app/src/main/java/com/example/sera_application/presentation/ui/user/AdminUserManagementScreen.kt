package com.example.sera_application.presentation.ui.user


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sera_application.ui.theme.SERA_ApplicationTheme

class AdminUserManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SERA_ApplicationTheme {
                AdminUserManagementScreen()
            }
        }
    }
}

data class User(
    val id: Int,
    val name: String,
    val email: String,
    var status: UserStatus,
    val memberSince: String
)

enum class UserStatus {
    ACTIVE, SUSPENDED, PENDING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen() {
    var users by remember {
        mutableStateOf(
            listOf(
                User(1, "Organizer1", "org1@gmail.com", UserStatus.ACTIVE, "Jan 07, 2023"),
                User(2, "Organizer2", "org2@gmail.com", UserStatus.SUSPENDED, "Mar 15, 2023"),
                User(3, "Organizer3", "org3@gmail.com", UserStatus.ACTIVE, "May 22, 2023"),
                User(4, "Organizer4", "org4@gmail.com", UserStatus.PENDING, "Jan 15, 2024"),
                User(5, "Organizer5", "org5@gmail.com", UserStatus.PENDING, "Feb 10, 2024"),
                User(6, "Organizer6", "org6@gmail.com", UserStatus.PENDING, "Mar 05, 2024")
            )
        )
    }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val filteredUsers = when (selectedFilter) {
        "Pending" -> users.filter { it.status == UserStatus.PENDING }
        else -> users
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2D2D2D)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF3F4F6))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminFilterButton(
                    text = "All",
                    isSelected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" },
                    modifier = Modifier.weight(1f)
                )
                AdminFilterButton(
                    text = "Pending Organizer",
                    isSelected = selectedFilter == "Pending",
                    onClick = { selectedFilter = "Pending" },
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    UserCard(
                        user = user,
                        onViewClick = {
                            selectedUser = user
                            showDialog = true
                        },
                        onDeleteClick = {
                            users = users.filter { it.id != user.id }
                        }
                    )
                }
            }
        }
    }

    if (showDialog && selectedUser != null) {
        UserDetailsDialog(
            user = selectedUser!!,
            onDismiss = { showDialog = false },
            onSuspend = {
                users = users.map {
                    if (it.id == selectedUser!!.id) it.copy(status = UserStatus.SUSPENDED)
                    else it
                }
                showDialog = false
            },
            onApprove = {
                users = users.map {
                    if (it.id == selectedUser!!.id) it.copy(status = UserStatus.ACTIVE)
                    else it
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun AdminFilterButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3B82F6) else Color(0xFFE5E7EB),
            contentColor = if (isSelected) Color.White else Color(0xFF374151)
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun UserCard(user: User, onViewClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFE5E7EB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = user.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "Organizer",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = user.email,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = user.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (user.status) {
                        UserStatus.ACTIVE -> Color(0xFF059669)
                        UserStatus.SUSPENDED -> Color(0xFFDC2626)
                        UserStatus.PENDING -> Color(0xFFEA580C)
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444)
                    )
                }
                Button(
                    onClick = onViewClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("View", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun UserDetailsDialog(
    user: User,
    onDismiss: () -> Unit,
    onSuspend: () -> Unit,
    onApprove: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFE5E7EB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Text(
                    text = user.email,
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280)
                )

                Text(
                    text = user.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (user.status) {
                        UserStatus.ACTIVE -> Color(0xFF059669)
                        UserStatus.SUSPENDED -> Color(0xFFDC2626)
                        UserStatus.PENDING -> Color(0xFFEA580C)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "Member Since: " + user.memberSince,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                )

                when (user.status) {
                    UserStatus.ACTIVE -> {
                        Button(
                            onClick = onSuspend,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Suspend", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    UserStatus.PENDING -> {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Approve", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    UserStatus.SUSPENDED -> {}
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem("Home", false)
            BottomNavItem("Event", false)
            BottomNavItem("Registration", false)
            BottomNavItem("Me", true)
        }
    }
}

@Composable
fun BottomNavItem(label: String, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF3B82F6) else Color(0xFF6B7280),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF6B7280)
        )
    }
}