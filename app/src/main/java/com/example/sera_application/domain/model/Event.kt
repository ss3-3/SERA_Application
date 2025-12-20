package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.domain.model.enums.EventStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Event(
    val eventId: String = "",
    val name: String,
    val organizerId: String,
    val organizerName: String,
    val description: String,
    val category: EventCategory,
    val status: EventStatus = EventStatus.PENDING,

    // Date & Time
    val date: Long,              // Format: "DD/MM/YYYY"
    val startTime: Long,         // Format: "HH:MM AM/PM"
    val endTime: Long,           // Format: "HH:MM AM/PM"

    // Location
    val location: String,          // Full: "Rimba, TARUMT (400 seats)"

    // Seats
    val rockZoneSeats: Int,
    val normalZoneSeats: Int,
    val totalSeats: Int,
    val availableSeats: Int,

    // Pricing
    val rockZonePrice: Double,
    val normalZonePrice: Double,

    // Media
    val imagePath: String? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Helper property to get price range string
     */
    val priceRange: String
        get() = when {
            rockZonePrice == 0.0 && normalZonePrice == 0.0 -> "Free"
            rockZonePrice == normalZonePrice -> "RM %.2f".format(rockZonePrice)
            else -> "RM %.2f - RM %.2f".format(
                minOf(rockZonePrice, normalZonePrice),
                maxOf(rockZonePrice, normalZonePrice)
            )
        }

    /**
     * Helper property to get full time range
     */
    val timeRange: String
        get() {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return "${format.format(Date(startTime))} - ${format.format(Date(endTime))}"
        }
}