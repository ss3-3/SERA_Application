package com.example.sera_application.utils.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sera_application.utils.permissions.hasNotificationPermission

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "sera_notifications"
    private const val CHANNEL_NAME = "SERA Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for events, reservations, and payments"

    /**
     * Create notification channel. Should be called early (e.g., in Application.onCreate)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableVibration(true)
                    enableLights(true)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create notification channel", e)
            }
        }
    }

    /**
     * Show a notification. Checks permission before showing.
     * @return true if notification was shown, false otherwise
     */
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ): Boolean {
        try {
            // Ensure channel is created
            createNotificationChannel(context)

            // Check if notifications are enabled
            val notificationManager = NotificationManagerCompat.from(context)
            if (!notificationManager.areNotificationsEnabled()) {
                Log.w(TAG, "Notifications are not enabled for this app")
                return false
            }

            // Check permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasNotificationPermission(context)) {
                    Log.w(TAG, "Notification permission not granted")
                    return false
                }
            }

            // Build notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with app icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))

            // Show notification
            notificationManager.notify(notificationId, builder.build())
            Log.d(TAG, "Notification shown successfully: $title")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when showing notification", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification", e)
            return false
        }
    }
}
