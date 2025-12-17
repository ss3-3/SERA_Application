package com.example.sera_application.data.remote.datasource

import com.example.sera_application.domain.model.Notification

interface NotificationRemoteDataSource {
    
    suspend fun sendNotification(notification: Notification): String // Returns notificationId
    
    suspend fun getNotificationsByUser(userId: String): List<Notification>
    
    suspend fun markNotificationAsRead(notificationId: String)
}