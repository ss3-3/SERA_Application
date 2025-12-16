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
        docRef.set(reservationWithId).await()
        return docRef.id
    }

    override suspend fun cancelReservation(reservationId: String) {
        reservationsRef.document(reservationId)
            .update("status", "CANCELLED")
            .await()
    }

    override suspend fun getReservationsByUser(userId: String): List<EventReservation> {
        val snapshot = reservationsRef
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(EventReservation::class.java) }
    }

    override suspend fun getReservationsByEvent(eventId: String): List<EventReservation> {
        val snapshot = reservationsRef
            .whereEqualTo("eventId", eventId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(EventReservation::class.java) }
    }
}
