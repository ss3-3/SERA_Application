package com.example.sera_application.data.remote.firebase

import android.util.Log
import com.example.sera_application.data.remote.datasource.ReservationRemoteDataSource
import com.example.sera_application.domain.model.EventReservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await



class FirebaseReservationDataSource(
    private val firestore: FirebaseFirestore
) : ReservationRemoteDataSource {


    private val reservationsRef = firestore.collection("reservations")


    override suspend fun createReservation(reservation: EventReservation): String {
        Log.d("ReservationDataSource", "Creating reservation for event: ${reservation.eventId}, seats: ${reservation.seats}")
        
        val docRef = if (reservation.reservationId.isBlank()) {
            reservationsRef.document()
        } else {
            reservationsRef.document(reservation.reservationId)
        }
        val reservationWithId = reservation.copy(reservationId = docRef.id)
        
        // Use a transaction to ensure atomic updates to both reservation and event
        firestore.runTransaction { transaction ->
            // Get the event document
            val eventRef = firestore.collection("events").document(reservation.eventId)
            val eventSnapshot = transaction.get(eventRef)
            
            if (!eventSnapshot.exists()) {
                Log.e("ReservationDataSource", "Event not found: ${reservation.eventId}")
                throw Exception("Event not found")
            }
            
            // Get current available seats
            val currentAvailableSeats = eventSnapshot.getLong("availableSeats")?.toInt() ?: 0
            Log.d("ReservationDataSource", "Current available seats: $currentAvailableSeats")
            
            // Check if enough seats are available
            if (currentAvailableSeats < reservation.seats) {
                Log.e("ReservationDataSource", "Not enough seats. Available: $currentAvailableSeats, Requested: ${reservation.seats}")
                throw Exception("Not enough available seats. Available: $currentAvailableSeats, Requested: ${reservation.seats}")
            }
            
            // Validate zone availability before creating reservation
            val currentRockZoneSeats = eventSnapshot.getLong("rockZoneSeats")?.toInt() ?: 0
            val currentNormalZoneSeats = eventSnapshot.getLong("normalZoneSeats")?.toInt() ?: 0
            
            // Check zone availability
            if (currentRockZoneSeats < reservation.rockZoneSeats) {
                throw Exception("Not enough Rock Zone seats. Available: $currentRockZoneSeats, Requested: ${reservation.rockZoneSeats}")
            }
            if (currentNormalZoneSeats < reservation.normalZoneSeats) {
                throw Exception("Not enough Normal Zone seats. Available: $currentNormalZoneSeats, Requested: ${reservation.normalZoneSeats}")
            }
            
            // Create the reservation
            transaction.set(docRef, reservationToMap(reservationWithId))
            
            // Update the available seats (total)
            val newAvailableSeats = currentAvailableSeats - reservation.seats
            Log.d("ReservationDataSource", "Updating available seats from $currentAvailableSeats to $newAvailableSeats")
            
            // Update zone-specific seats
            val newRockZoneSeats = currentRockZoneSeats - reservation.rockZoneSeats
            val newNormalZoneSeats = currentNormalZoneSeats - reservation.normalZoneSeats
            Log.d("ReservationDataSource", "Updating rockZone from $currentRockZoneSeats to $newRockZoneSeats")
            Log.d("ReservationDataSource", "Updating normalZone from $currentNormalZoneSeats to $newNormalZoneSeats")
            
            // Update all seat counts in one operation
            transaction.update(eventRef, mapOf(
                "availableSeats" to newAvailableSeats,
                "rockZoneSeats" to newRockZoneSeats,
                "normalZoneSeats" to newNormalZoneSeats
            ))
            
        }.await()
        
        Log.d("ReservationDataSource", "Reservation created successfully: ${docRef.id}")
        return docRef.id
    }


    override suspend fun cancelReservation(reservationId: String) {
        // Use a transaction to ensure atomic updates to both reservation and event
        firestore.runTransaction { transaction ->
            // Get the reservation document
            val reservationRef = reservationsRef.document(reservationId)
            val reservationSnapshot = transaction.get(reservationRef)
            
            if (!reservationSnapshot.exists()) {
                throw Exception("Reservation not found")
            }
            
            // Get reservation details
            val eventId = reservationSnapshot.getString("eventId") ?: throw Exception("Event ID not found")
            val seats = reservationSnapshot.getLong("seats")?.toInt() ?: 0
            val rockZoneSeats = reservationSnapshot.getLong("rockZoneSeats")?.toInt() ?: 0
            val normalZoneSeats = reservationSnapshot.getLong("normalZoneSeats")?.toInt() ?: 0
            
            // Get the event document
            val eventRef = firestore.collection("events").document(eventId)
            val eventSnapshot = transaction.get(eventRef)
            
            if (!eventSnapshot.exists()) {
                throw Exception("Event not found")
            }
            
            // Update reservation status
            transaction.update(reservationRef, "status", com.example.sera_application.domain.model.enums.ReservationStatus.CANCELLED.name)
            
            // Restore the available seats (total)
            val currentAvailableSeats = eventSnapshot.getLong("availableSeats")?.toInt() ?: 0
            val newAvailableSeats = currentAvailableSeats + seats
            
            // Restore zone-specific seats
            val currentRockZone = eventSnapshot.getLong("rockZoneSeats")?.toInt() ?: 0
            val currentNormalZone = eventSnapshot.getLong("normalZoneSeats")?.toInt() ?: 0
            val newRockZoneSeats = currentRockZone + rockZoneSeats
            val newNormalZoneSeats = currentNormalZone + normalZoneSeats
            
            Log.d("ReservationDataSource", "Restoring seats - Available: $currentAvailableSeats -> $newAvailableSeats")
            Log.d("ReservationDataSource", "Restoring rockZone: $currentRockZone -> $newRockZoneSeats")
            Log.d("ReservationDataSource", "Restoring normalZone: $currentNormalZone -> $newNormalZoneSeats")
            
            // Update all seat counts in one operation
            transaction.update(eventRef, mapOf(
                "availableSeats" to newAvailableSeats,
                "rockZoneSeats" to newRockZoneSeats,
                "normalZoneSeats" to newNormalZoneSeats
            ))
            
        }.await()
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
