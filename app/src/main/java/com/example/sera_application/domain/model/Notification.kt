package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.NotificationType
import java.time.LocalDateTime

data class Notification(
    val id: String,
    val userId: String, // who receives it
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedEventId: String? = null, // optional: event id
    val relatedPaymentId: String? = null,
    val relatedReservationId: String? = null,
    val isRead: Boolean = false,
    val createdAt: LocalDateTime
)
