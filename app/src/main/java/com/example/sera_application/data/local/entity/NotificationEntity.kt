package com.example.sera_application.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey
    @ColumnInfo(name = "notification_id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,                 // Who receives this notification

    @ColumnInfo(name = "title")
    val title: String,                  // Notification title (e.g., "Event Updated")

    @ColumnInfo(name = "message")
    val message: String,                // Notification body/content

    @ColumnInfo(name = "type")
    val type: String,                   // "EVENT_UPDATE", "PAYMENT", "RESERVATION", "SYSTEM"

    @ColumnInfo(name = "related_event_id")
    val relatedEventId: String? = null, // Optional: links to event if notification is about an event

    @ColumnInfo(name = "related_payment_id")
    val relatedPaymentId: String? = null, // Optional: links to payment if notification is about payment

    @ColumnInfo(name = "related_reservation_id")
    val relatedReservationId: String? = null, // Optional: links to reservation for participant-specific notifications

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,        // false = unread (show badge), true = read

    @ColumnInfo(name = "created_at")
    val createdAt: Long                 // Timestamp in milliseconds (System.currentTimeMillis())


)