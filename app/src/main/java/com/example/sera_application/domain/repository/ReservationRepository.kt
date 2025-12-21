package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.ReservationStatus
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {

    suspend fun createReservation(reservation: EventReservation): Result<String> // Returns reservationId

    suspend fun cancelReservation(reservationId: String): Result<Unit>

    fun getUserReservations(userId: String): Flow<List<EventReservation>>

    fun getEventReservations(eventId: String): Flow<List<EventReservation>>

    suspend fun getAllReservations(): List<EventReservation>

    suspend fun getReservationById(reservationId: String): EventReservation?

    suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Result<Unit>
}