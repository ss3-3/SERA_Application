package com.example.sera_application.domain.model.enums

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}

data class Reservation(
    val reservationId: String,
    val eventId: String,
    val userId: String,
//    val seats: List<Seat>,
    val status: ReservationStatus,
    val createdAt: Long,
    val paymentMethod: String? = null,
    val transactionId: String? = null
)
