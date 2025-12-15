package com.example.sera_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.sera_application.data.local.Converters
import com.example.sera_application.domain.model.enums.NotificationType
import org.threeten.bp.LocalDateTime

@Entity(tableName = "notifications")
@TypeConverters(Converters::class)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedEventId: String? = null,
    val relatedPaymentId: String? = null,
    val isRead: Boolean = false,
    val createdAt: LocalDateTime
)