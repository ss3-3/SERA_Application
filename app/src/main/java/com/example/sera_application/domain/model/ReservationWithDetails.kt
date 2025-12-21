package com.example.sera_application.domain.model

data class ReservationWithDetails(
    val reservation: EventReservation,
    val event: Event?,
    val paymentId: String? = null
)
