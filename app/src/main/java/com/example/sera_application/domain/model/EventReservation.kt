package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.ReservationStatus

data class EventReservation(
    val reservationId: String,
    val eventId: String,
    val userId: String,
    val zoneId: String,
    val zoneName: String,
    val quantity: Int,
    val seatNumbers: String,
    val pricePerSeat: Double,
    val totalPrice: Double,
    val status: ReservationStatus,
    val createdAt: Long,
    // Event details (for display)
    val eventName: String = "",
    val eventDate: String = "",
    val eventTime: String = "",
    val venue: String = "",
    // User details (for organizer view)
    val participantName: String = "",
    val participantEmail: String = ""
)
