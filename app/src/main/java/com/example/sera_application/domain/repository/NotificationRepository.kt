package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.NotificationMessage
import com.example.sera_application.domain.model.enums.NotificationType

interface NotificationRepository {
    /**
     * Send a notification to a specific user.
     * This may trigger:
     * - Firebase Cloud Messaging (remote)
     * - Local notification display
     * - Optional local persistence (history)
     */
    suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        referenceId: String? = null
    ): Result<Unit>

    /**
     * Get all notifications for a user (history).
     */
    suspend fun getNotificationsByUser(
        userId: String
    ): Result<List<NotificationMessage>>

    /**
     * Mark a notification as read.
     */
    suspend fun markAsRead(
        notificationId: String
    ): Result<Unit>

    /**
     * Clear all notifications for a user (optional).
     */
    suspend fun clearAll(
        userId: String
    ): Result<Unit>
}