package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.NotificationType

data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedEventId: String? = null,
    val relatedReservationId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long
)