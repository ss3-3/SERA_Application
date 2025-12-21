package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.ReservationStatistics

interface ReportRepository {
    suspend fun getReservationStatistics(eventId: String? = null): ReservationStatistics
    suspend fun getTotalRevenue(eventId: String? = null): Double
    suspend fun getEventStatistics(eventId: String): ReservationStatistics
    suspend fun getUserStatistics(userId: String): ReservationStatistics
}

