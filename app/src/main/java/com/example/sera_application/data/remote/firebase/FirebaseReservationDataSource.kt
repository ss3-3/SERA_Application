package com.example.sera_application.data.remote.firebase

import com.example.sera_application.data.remote.datasource.ReservationRemoteDataSource
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseReservationDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReservationRemoteDataSource {

    private val reservationsRef = firestore.collection("reservations")

    override suspend fun createReservation(reservation: EventReservation): Result<String> {
        return try {
            val docRef = if (reservation.reservationId.isBlank()) {
                reservationsRef.document()
            } else {
                reservationsRef.document(reservation.reservationId)
            }
            val reservationWithId = reservation.copy(reservationId = docRef.id)
            docRef.set(reservationToMap(reservationWithId)).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelReservation(reservationId: String): Result<Unit> {
        return try {
            reservationsRef.document(reservationId)
                .update("status", ReservationStatus.CANCELLED.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun fetchUserReservations(userId: String): Flow<List<EventReservation>> {
        return callbackFlow {
            val listener = reservationsRef
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    
                    val reservations = snapshot?.documents?.mapNotNull { it.toReservation() } ?: emptyList()
                    trySend(reservations)
                }
            
            awaitClose { listener.remove() }
        }
    }

    override fun fetchEventReservations(eventId: String): Flow<List<EventReservation>> {
        return callbackFlow {
            val listener = reservationsRef
                .whereEqualTo("eventId", eventId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    
                    val reservations = snapshot?.documents?.mapNotNull { it.toReservation() } ?: emptyList()
                    trySend(reservations)
                }
            
            awaitClose { listener.remove() }
        }
    }

    override suspend fun getAllReservations(): List<EventReservation> {
        val snapshot = reservationsRef.get().await()
        return snapshot.documents.mapNotNull { it.toReservation() }
    }

    override suspend fun getReservationById(reservationId: String): EventReservation? {
        val snapshot = reservationsRef.document(reservationId).get().await()
        return snapshot.toReservation()
    }

    override suspend fun updateReservationStatus(reservationId: String, status: ReservationStatus): Result<Unit> {
        return try {
            reservationsRef.document(reservationId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun reservationToMap(reservation: EventReservation): Map<String, Any?> {
        return mapOf(
            "reservationId" to reservation.reservationId,
            "eventId" to reservation.eventId,
            "userId" to reservation.userId,
            "seats" to reservation.seats,
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
                totalPrice = getDouble("totalPrice") ?: 0.0,
                status = status,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
