package com.example.sera_application.data.repository

import android.content.Context
import android.util.Log
import com.example.sera_application.data.remote.datasource.NotificationRemoteDataSource
import com.example.sera_application.domain.model.Notification
import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.repository.NotificationRepository
import com.example.sera_application.utils.notifications.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationRemoteDataSource,
    @ApplicationContext private val context: Context
) : NotificationRepository {

    override suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedEventId: String?,
        relatedReservationId: String?
    ): Result<Unit> {
        return try {
            val notification = Notification(
                id = "",
                userId = userId,
                title = title,
                message = message,
                type = type,
                relatedEventId = relatedEventId,
                relatedReservationId = relatedReservationId,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            // Save to Firebase
            val notificationId = remoteDataSource.sendNotification(notification)
            
            // Show local notification immediately (if permission granted)
            val notificationShown = NotificationHelper.showNotification(
                context = context,
                notificationId = notificationId.hashCode(), // Use hash as notification ID
                title = title,
                message = message
            )
            
            if (!notificationShown) {
                Log.w("NotificationRepository", "Failed to show local notification - permission may not be granted")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserNotifications(userId: String): Result<List<Notification>> {
        return try {
            val notifications = remoteDataSource.getNotificationsByUser(userId)
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            remoteDataSource.markNotificationAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
