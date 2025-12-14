package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.ReservationStatus

data class EventReservation(
    val reservationId: String,
    val eventId: String,
    val userId: String,
    val seats: Int,
    val status: ReservationStatus,
    val createdAt: Long
)
