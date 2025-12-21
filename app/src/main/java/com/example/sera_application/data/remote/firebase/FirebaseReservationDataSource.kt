package com.example.sera_application.data.remote.firebase


import com.example.sera_application.data.remote.datasource.ReservationRemoteDataSource
import com.example.sera_application.domain.model.EventReservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class FirebaseReservationDataSource(
    private val firestore: FirebaseFirestore
) : ReservationRemoteDataSource {


    private val reservationsRef = firestore.collection("reservations")


    override suspend fun createReservation(reservation: EventReservation): String {
        val docRef = if (reservation.reservationId.isBlank()) {
            reservationsRef.document()
        } else {
            reservationsRef.document(reservation.reservationId)
        }
        val reservationWithId = reservation.copy(reservationId = docRef.id)
        docRef.set(reservationToMap(reservationWithId)).await()
        return docRef.id
    }


    override suspend fun cancelReservation(reservationId: String) {
        reservationsRef.document(reservationId)
            .update("status", com.example.sera_application.domain.model.enums.ReservationStatus.CANCELLED.name)
            .await()
    }


    override suspend fun getReservationsByUser(userId: String): List<EventReservation> {
        val snapshot = reservationsRef
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toReservation() }
    }


    override suspend fun getReservationsByEvent(eventId: String): List<EventReservation> {
        val snapshot = reservationsRef
            .whereEqualTo("eventId", eventId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toReservation() }
    }


    override suspend fun getAllReservations(): List<EventReservation> {
        val snapshot = reservationsRef.get().await()
        return snapshot.documents.mapNotNull { it.toReservation() }
    }


    override suspend fun getReservationById(reservationId: String): EventReservation? {
        val snapshot = reservationsRef.document(reservationId).get().await()
        return snapshot.toReservation()
    }


    override suspend fun updateReservationStatus(reservationId: String, status: String) {
        reservationsRef.document(reservationId)
            .update("status", status)
            .await()
    }


    private fun reservationToMap(reservation: EventReservation): Map<String, Any?> {
        return mapOf(
            "reservationId" to reservation.reservationId,
            "eventId" to reservation.eventId,
            "userId" to reservation.userId,
            "seats" to reservation.seats,
            "rockZoneSeats" to reservation.rockZoneSeats,
            "normalZoneSeats" to reservation.normalZoneSeats,
            "totalPrice" to reservation.totalPrice,
            "status" to reservation.status.name,
            "createdAt" to reservation.createdAt
        )
    }


    private fun com.google.firebase.firestore.DocumentSnapshot.toReservation(): EventReservation? {
        if (!exists()) return null
        return try {
            val statusStr = getString("status") ?: "PENDING"
            val status = com.example.sera_application.domain.model.enums.ReservationStatus.valueOf(statusStr)

            EventReservation(
                reservationId = getString("reservationId") ?: id,
                eventId = getString("eventId") ?: "",
                userId = getString("userId") ?: "",
                seats = getLong("seats")?.toInt() ?: 0,
                rockZoneSeats = getLong("rockZoneSeats")?.toInt() ?: 0,
                normalZoneSeats = getLong("normalZoneSeats")?.toInt() ?: 0,
                totalPrice = getDouble("totalPrice") ?: 0.0,
                status = status,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
