package com.example.sera_application.data.repository

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.repository.ReservationRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepositoryImpl @Inject constructor() : ReservationRepository {

    // In-memory storage for mock data
    private val reservations = mutableListOf<EventReservation>()

    init {
        // Initialize with sample data
        reservations.addAll(getSampleReservations())
    }

    override suspend fun createReservation(reservation: EventReservation): Boolean {
        // Simulate network delay
        delay(500)
        
        // Check for duplicate
        val exists = reservations.any { it.reservationId == reservation.reservationId }
        if (exists) {
            return false
        }
        
        reservations.add(reservation)
        return true
    }

    override suspend fun cancelReservation(reservationId: String): Boolean {
        delay(300)
        
        val reservation = reservations.find { it.reservationId == reservationId }
        if (reservation != null) {
            val index = reservations.indexOf(reservation)
            reservations[index] = reservation.copy(status = ReservationStatus.CANCELLED)
            return true
        }
        return false
    }

    override suspend fun getUserReservations(userId: String): List<EventReservation> {
        delay(400)
        
        return if (userId.isBlank()) {
            reservations.toList()
        } else {
            reservations.filter { it.userId == userId }
        }
    }

    override suspend fun getEventReservations(eventId: String): List<EventReservation> {
        delay(400)
        
        return reservations.filter { it.eventId == eventId }
    }

    override suspend fun updateReservationStatus(reservationId: String, status: String): Boolean {
        delay(300)
        
        val reservation = reservations.find { it.reservationId == reservationId }
        if (reservation != null) {
            val newStatus = try {
                ReservationStatus.valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                return false
            }
            
            val index = reservations.indexOf(reservation)
            reservations[index] = reservation.copy(status = newStatus)
            return true
        }
        return false
    }

    // Helper method to get sample data
    private fun getSampleReservations(): List<EventReservation> {
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        return listOf(
            EventReservation(
                reservationId = "RES-001",
                eventId = "EVENT-001",
                userId = "USER-001",
                zoneId = "ZONE-A",
                zoneName = "VIP Section",
                quantity = 2,
                seatNumbers = "A1, A2",
                pricePerSeat = 150.0,
                totalPrice = 300.0,
                status = ReservationStatus.CONFIRMED,
                createdAt = currentTime - (5 * oneDayMillis),
                eventName = "Tech Conference 2024",
                eventDate = "2024-12-20",
                eventTime = "09:00 AM",
                venue = "Convention Center",
                participantName = "John Doe",
                participantEmail = "john@example.com"
            ),
            EventReservation(
                reservationId = "RES-002",
                eventId = "EVENT-001",
                userId = "USER-002",
                zoneId = "ZONE-B",
                zoneName = "General Admission",
                quantity = 4,
                seatNumbers = "B10, B11, B12, B13",
                pricePerSeat = 75.0,
                totalPrice = 300.0,
                status = ReservationStatus.CONFIRMED,
                createdAt = currentTime - (3 * oneDayMillis),
                eventName = "Tech Conference 2024",
                eventDate = "2024-12-20",
                eventTime = "09:00 AM",
                venue = "Convention Center",
                participantName = "Jane Smith",
                participantEmail = "jane@example.com"
            ),
            EventReservation(
                reservationId = "RES-003",
                eventId = "EVENT-002",
                userId = "USER-001",
                zoneId = "ZONE-A",
                zoneName = "Front Row",
                quantity = 3,
                seatNumbers = "FR1, FR2, FR3",
                pricePerSeat = 200.0,
                totalPrice = 600.0,
                status = ReservationStatus.PENDING,
                createdAt = currentTime - oneDayMillis,
                eventName = "Music Festival",
                eventDate = "2024-12-25",
                eventTime = "06:00 PM",
                venue = "City Stadium",
                participantName = "John Doe",
                participantEmail = "john@example.com"
            ),
            EventReservation(
                reservationId = "RES-004",
                eventId = "EVENT-003",
                userId = "USER-003",
                zoneId = "ZONE-C",
                zoneName = "Balcony",
                quantity = 2,
                seatNumbers = "BAL-5, BAL-6",
                pricePerSeat = 50.0,
                totalPrice = 100.0,
                status = ReservationStatus.CANCELLED,
                createdAt = currentTime - (10 * oneDayMillis),
                eventName = "Theater Show",
                eventDate = "2024-12-15",
                eventTime = "07:30 PM",
                venue = "Grand Theater",
                participantName = "Bob Wilson",
                participantEmail = "bob@example.com"
            ),
            EventReservation(
                reservationId = "RES-005",
                eventId = "EVENT-001",
                userId = "USER-004",
                zoneId = "ZONE-A",
                zoneName = "VIP Section",
                quantity = 1,
                seatNumbers = "A15",
                pricePerSeat = 150.0,
                totalPrice = 150.0,
                status = ReservationStatus.CONFIRMED,
                createdAt = currentTime - (2 * oneDayMillis),
                eventName = "Tech Conference 2024",
                eventDate = "2024-12-20",
                eventTime = "09:00 AM",
                venue = "Convention Center",
                participantName = "Alice Brown",
                participantEmail = "alice@example.com"
            )
        )
    }

    // Additional helper methods for testing
    fun clearAllReservations() {
        reservations.clear()
    }

    fun addSampleReservation(reservation: EventReservation) {
        reservations.add(reservation)
    }

    fun getReservationCount(): Int = reservations.size
}
