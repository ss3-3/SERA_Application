package com.example.sera_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.domain.model.enums.EventStatus

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
    val updatedAt: Long,

    // add some fields for event details
    val category: EventCategory,
    val vipSeats: Int,
    val normalSeats: Int,
    val availableSeats: Int,
//    val priceRange: String
)

//data class Event(
//    @PrimaryKey
//    val eventId: String = "",
//    val name: String,
//    val organizerId: String,
//    val organizerName: String,
//    val description: String,
//    val category: EventCategory,
//    val status: EventStatus = EventStatus.PENDING,
//
//    // Date & Time
//    val date: String,              // Format: "DD/MM/YYYY"
//    val startTime: String,         // Format: "HH:MM AM/PM"
//    val endTime: String,           // Format: "HH:MM AM/PM"
//    val duration: String,          // Format: "X hour(s)"
//
//    // Location
//    val location: String,          // Full: "Rimba, TARUMT (400 seats)"
//
//    // Seats
//    val rockZoneSeats: Int,
//    val normalZoneSeats: Int,
//    val totalSeats: Int,
//    val availableSeats: Int,
//
//    // Pricing
//    val rockZonePrice: Double,
//    val normalZonePrice: Double,
//
//    // Media
//    val imagePath: String? = null,
//
//    // Timestamps
//    val createdAt: Long = System.currentTimeMillis(),
//    val updatedAt: Long = System.currentTimeMillis()
//)