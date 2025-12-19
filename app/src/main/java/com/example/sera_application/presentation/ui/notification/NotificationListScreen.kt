package com.example.sera_application.presentation.ui.notification

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Notification types for different events
 */
enum class NotificationType(val icon: ImageVector) {
    RESERVATION_CONFIRMED(Icons.Default.CheckCircle),
    PAYMENT_SUCCESS(Icons.Default.Payment),
    EVENT_UPDATE(Icons.Default.Event),
    EVENT_REMINDER(Icons.Default.Notifications),
    EVENT_CANCELLED(Icons.Default.Cancel)
}

/**
 * Notification data model for UI
 */
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: String,     // e.g., "Just now", "2 hours ago"
    val isRead: Boolean = false
)

/**
 * Notification List Screen
 * Full-page notification list for participants
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    onBackClick: () -> Unit = {},
    onNotificationDelete: (String) -> Unit = {},
    onNotificationClick: (NotificationItem) -> Unit = {}
) {
    // TODO: Get notifications from ViewModel
    var notifications by remember {
        mutableStateOf(getSampleNotifications())
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            // New Notification Header
            if (unreadCount > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "New Notification($unreadCount)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Notification List
            if (notifications.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No notifications yet",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onDelete = {
                                onNotificationDelete(notification.id)
                                notifications = notifications.filter { it.id != notification.id }
                            },
                            onClick = {
                                onNotificationClick(notification)
                                // Mark as read
                                notifications = notifications.map {
                                    if (it.id == notification.id) it.copy(isRead = true) else it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Notification Card
 */
@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF0F8FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getNotificationIconColor(notification.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.type.icon,
                    contentDescription = null,
                    tint = getNotificationIconColor(notification.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Notification Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.timestamp,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Button
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Notification") },
            text = { Text("Are you sure you want to delete this notification?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Get icon color based on notification type
 */
private fun getNotificationIconColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.RESERVATION_CONFIRMED -> Color(0xFF4CAF50)
        NotificationType.PAYMENT_SUCCESS -> Color(0xFF2196F3)
        NotificationType.EVENT_UPDATE -> Color(0xFFFF9800)
        NotificationType.EVENT_REMINDER -> Color(0xFF9C27B0)
        NotificationType.EVENT_CANCELLED -> Color(0xFFF44336)
    }
}

/**
 * Sample data for testing
 * TODO: Replace with ViewModel data
 */
private fun getSampleNotifications(): List<NotificationItem> {
    return listOf(
        NotificationItem(
            id = "1",
            type = NotificationType.RESERVATION_CONFIRMED,
            title = "Reservation Confirmed!",
            message = "Your reservation for MUSIC FIESTA 6.0 has been confirmed!",
            timestamp = "Just now",
            isRead = false
        ),
        NotificationItem(
            id = "2",
            type = NotificationType.RESERVATION_CONFIRMED,
            title = "Reservation Confirmed!",
            message = "Your reservation for GOTAR Festival has been confirmed!",
            timestamp = "2 hours ago",
            isRead = false
        ),
        NotificationItem(
            id = "3",
            type = NotificationType.PAYMENT_SUCCESS,
            title = "Payment Successful!",
            message = "Your payment for MUSIC FIESTA 6.0 has been processed successfully!",
            timestamp = "5 hours ago",
            isRead = false
        ),
        NotificationItem(
            id = "4",
            type = NotificationType.PAYMENT_SUCCESS,
            title = "Payment Successful!",
            message = "Your payment for VOICHESTRA has been processed successfully!",
            timestamp = "1 day ago",
            isRead = false
        ),
        NotificationItem(
            id = "5",
            type = NotificationType.EVENT_REMINDER,
            title = "Coming Soon!",
            message = "MUSIC FIESTA 6.0 is happening tomorrow at 7:00 PM!",
            timestamp = "2 days ago",
            isRead = false
        )
    )
}

// ==================== PREVIEW ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotificationListScreenPreview() {
    NotificationListScreen()
}

@Preview(showBackground = true)
@Composable
private fun NotificationCardPreview() {
    NotificationCard(
        notification = NotificationItem(
            id = "1",
            type = NotificationType.RESERVATION_CONFIRMED,
            title = "Reservation Confirmed!",
            message = "Your reservation for MUSIC FIESTA 6.0 has been confirmed!",
            timestamp = "Just now",
            isRead = false
        ),
        onDelete = {},
        onClick = {}
    )
}