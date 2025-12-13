package com.example.sera_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val eventId: String,
    val organizerId: String,
    val title: String,
    val description: String,
    val location: String,
    val dateTime: Long, // Timestamp
    val imagePath: String?,
    val capacity: Int,
    val status: String, // Store as String, convert to/from EventStatus enum
    val createdAt: Long,
    val updatedAt: Long
)