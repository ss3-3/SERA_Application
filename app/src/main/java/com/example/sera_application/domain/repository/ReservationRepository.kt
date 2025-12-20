package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.EventReservation

interface ReservationRepository {

    suspend fun createReservation(reservation: EventReservation): String?

    suspend fun cancelReservation(reservationId: String): Boolean

    suspend fun getUserReservations(userId: String): List<EventReservation>

    suspend fun getEventReservations(eventId: String): List<EventReservation>

    suspend fun getAllReservations(): List<EventReservation>

    suspend fun getReservationById(reservationId: String): EventReservation?

    suspend fun updateReservationStatus(
        reservationId: String,
        status: String
    ): Boolean
}