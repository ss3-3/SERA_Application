package com.example.sera_application.domain.repository

import com.example.sera_application.data.local.UserParticipation
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.ReservationStatus
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {

    suspend fun createReservation(reservation: EventReservation): Result<String>

    suspend fun cancelReservation(reservationId: String): Result<Unit>

    suspend fun getUserReservations(userId: String): Flow<List<EventReservation>>

    suspend fun getEventReservations(eventId: String): Flow<List<EventReservation>>

    suspend fun getAllReservations(): List<EventReservation>

    suspend fun getReservationById(reservationId: String): EventReservation?

    suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Result<Unit>

    // Add
    suspend fun getTotalReservationCount(): Int
    suspend fun getUniqueParticipantsCount(): Int
    suspend fun getTotalParticipantsByEvents(eventIds: List<String>): Int
    suspend fun getParticipantsByEvent(eventId: String): Int
    suspend fun getTopParticipants(limit: Int): List<UserParticipation>
    suspend fun getMonthlyReservationTrend(startDate: Long): List<Int>
}