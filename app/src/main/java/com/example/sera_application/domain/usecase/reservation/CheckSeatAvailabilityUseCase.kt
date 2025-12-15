package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class CheckSeatAvailabilityUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(
        eventId: String,
        requestedQuantity: Int,
        eventCapacity: Int
    ): Result<Boolean> {
        return try {
            // Get all reservations for the event
            val existingReservations = reservationRepository.getEventReservations(eventId)
            
            // Calculate total reserved seats
            val totalReserved = existingReservations
                .filter { it.status.name != "CANCELLED" }
                .sumOf { it.quantity }
            
            // Check if enough seats available
            val availableSeats = eventCapacity - totalReserved
            val isAvailable = availableSeats >= requestedQuantity
            
            if (isAvailable) {
                Result.success(true)
            } else {
                Result.failure(Exception("Only $availableSeats seats available, but $requestedQuantity requested"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
