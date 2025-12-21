package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.Notification
import com.example.sera_application.domain.model.enums.NotificationType

interface NotificationRepository {
    suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedEventId: String? = null,
        relatedReservationId: String? = null
    ): Result<Unit>

    suspend fun getUserNotifications(userId: String): Result<List<Notification>>

    suspend fun markAsRead(notificationId: String): Result<Unit>
}