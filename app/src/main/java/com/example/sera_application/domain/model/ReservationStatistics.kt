package com.example.sera_application.domain.model

data class ReservationStatistics(
    val totalReservations: Int,
    val confirmedReservations: Int,
    val cancelledReservations: Int,
    val pendingReservations: Int,
    val totalRevenue: Double,
    val totalSeatsReserved: Int,
    val averageSeatsPerReservation: Double,
    val cancellationRate: Double
)
