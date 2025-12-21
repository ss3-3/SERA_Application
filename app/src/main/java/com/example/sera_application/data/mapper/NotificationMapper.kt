package com.example.sera_application.data.mapper

import com.example.sera_application.domain.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

object NotificationMapper {
    fun notificationToFirestoreMap(notification: Notification): Map<String, Any?> {
        return mapOf(
            "id" to notification.id,
            "userId" to notification.userId,
            "title" to notification.title,
            "message" to notification.message,
            "type" to notification.type.name,
            "relatedEventId" to notification.relatedEventId,
            "relatedReservationId" to notification.relatedReservationId,
            "isRead" to notification.isRead,
            "createdAt" to Timestamp(notification.createdAt / 1000, ((notification.createdAt % 1000) * 1_000_000).toInt())
        )
    }
}

fun DocumentSnapshot.toNotification(): Notification? {
    return try {
        val data = this.data ?: return null
        val timestamp = data["createdAt"] as? Timestamp
        val createdAt = timestamp?.toDate()?.time ?: System.currentTimeMillis()

        Notification(
            id = this.id,
            userId = data["userId"]?.toString() ?: "",
            title = data["title"]?.toString() ?: "",
            message = data["message"]?.toString() ?: "",
            type = try {
                com.example.sera_application.domain.model.enums.NotificationType.valueOf(
                    data["type"]?.toString() ?: "SYSTEM"
                )
            } catch (e: IllegalArgumentException) {
                com.example.sera_application.domain.model.enums.NotificationType.SYSTEM
            },
            relatedEventId = data["relatedEventId"]?.toString(),
            relatedReservationId = data["relatedReservationId"]?.toString(),
            isRead = data["isRead"] as? Boolean ?: false,
            createdAt = createdAt
        )
    } catch (e: Exception) {
        null
    }
}
