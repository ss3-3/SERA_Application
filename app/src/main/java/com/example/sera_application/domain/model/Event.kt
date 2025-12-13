package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.EventStatus

data class Event(
    val eventId: String,
    val organizerId: String,
    val title: String,
    val description: String,
    val location: String,
    val dateTime: Long,           // store as timestamp
    val imagePath: String?,
    val capacity: Int,
    val status: EventStatus,
    val createdAt: Long,
    val updatedAt: Long
)