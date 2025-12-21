package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.ReservationStatus

data class EventReservation(
    val reservationId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val seats: Int = 0,
    val rockZoneSeats: Int = 0,
    val normalZoneSeats: Int = 0,
    val totalPrice: Double = 0.0,
    val status: ReservationStatus = ReservationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)