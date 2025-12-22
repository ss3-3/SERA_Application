package com.example.sera_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sera_application.domain.model.enums.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,                 // who receives it
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedEventId: String? = null, // optional: event update
    val relatedPaymentId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long // Store as Long timestamp (consistent with other entities)
)