package com.example.sera_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sera_application.domain.model.enums.EventCategory

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val eventId: String,
    val eventName: String,
    val organizerId: String, // UserId => organizer role (from id get username)
    val description: String,
    val category: EventCategory,
    val status: String, // Store as String, convert to/from EventStatus enum

    val createdAt: Long,
    val updatedAt: Long,
    val date: Long,
    val startTime: Long,
    val endTime: Long,

    val location: String,

    val rockZoneSeats: Int,
    val normalZoneSeats: Int,
    val totalSeats: Int,
    val availableSeats: Int,

    val rockZonePrice: Double,
    val normalZonePrice: Double,

    val imagePath: String? = null,
)