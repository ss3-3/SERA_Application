package com.example.sera_application.presentation.ui.notification

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Notification Toast Types
enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

// Toast notification data
data class ToastNotification(
    val title: String,
    val message: String,
    val type: ToastType = ToastType.SUCCESS,
    val duration: Long = 3000L  // milliseconds
)

/**
 * Notification Toast Component
 * Shows a temporary pop-up notification (like Snackbar but more customizable)
 *
 * Usage example:
 * ```
 * var showToast by remember { mutableStateOf(false) }
 * var toastData by remember { mutableStateOf<ToastNotification?>(null) }
 *
 * NotificationToast(
 *     show = showToast,
 *     notification = toastData,
 *     onDismiss = { showToast = false }
 * )
 *
 * // Trigger toast:
 * toastData = ToastNotification(
 *     title = "Event Created",
 *     message = "Your event has been successfully created!",
 *     type = ToastType.SUCCESS
 * )
 * showToast = true
 * ```
 */
@Composable
fun NotificationToast(
    show: Boolean,
    notification: ToastNotification?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss after duration
    LaunchedEffect(show) {
        if (show && notification != null) {
            delay(notification.duration)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = show && notification != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        notification?.let {
            ToastContent(notification = it)
        }
    }
}

// Toast Content UI
@Composable
private fun ToastContent(notification: ToastNotification) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getToastColor(notification.type).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getToastIcon(notification.type),
                    contentDescription = null,
                    tint = getToastColor(notification.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Just now",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Get icon based on toast type
private fun getToastIcon(type: ToastType) = when (type) {
    ToastType.SUCCESS -> Icons.Default.CheckCircle
    ToastType.ERROR -> Icons.Default.Error
    ToastType.INFO -> Icons.Default.Info
    ToastType.WARNING -> Icons.Default.Warning
}

// Get color based on toast type
private fun getToastColor(type: ToastType) = when (type) {
    ToastType.SUCCESS -> Color(0xFF4CAF50)
    ToastType.ERROR -> Color(0xFFF44336)
    ToastType.INFO -> Color(0xFF2196F3)
    ToastType.WARNING -> Color(0xFFFF9800)
}

// ==================== PREVIEW ====================

@Preview(showBackground = true)
@Composable
private fun NotificationToastPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        ToastContent(
            notification = ToastNotification(
                title = "Event Created",
                message = "Your event \"MUSIC FIESTA 6.0\" has been successfully created!",
                type = ToastType.SUCCESS
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationToastErrorPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        ToastContent(
            notification = ToastNotification(
                title = "Event Creation Failed",
                message = "Unable to create event. Please try again.",
                type = ToastType.ERROR
            )
        )
    }
}