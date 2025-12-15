package com.example.sera_application.domain.model

data class ReservationRequest(
    val eventId: String,
    val userId: String,
    val zoneId: String,
    val zoneName: String,
    val quantity: Int,
    val seatNumbers: String,
    val pricePerSeat: Double,
    val discountPercentage: Double = 0.0
)
