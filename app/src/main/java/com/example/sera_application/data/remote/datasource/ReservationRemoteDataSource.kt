package com.example.sera_application.data.remote.datasource

import com.example.sera_application.domain.model.EventReservation

interface ReservationRemoteDataSource {
    
    suspend fun createReservation(reservation: EventReservation): String // Returns reservationId
    
    suspend fun cancelReservation(reservationId: String)
    
    suspend fun getReservationsByUser(userId: String): List<EventReservation>
    
    suspend fun getReservationsByEvent(eventId: String): List<EventReservation>
}