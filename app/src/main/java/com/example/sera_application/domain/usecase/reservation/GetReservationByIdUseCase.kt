package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class GetReservationByIdUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservationId: String): Result<EventReservation> {
        return try {
            if (reservationId.isBlank()) {
                return Result.failure(Exception("Reservation ID cannot be empty"))
            }

            val userReservations = reservationRepository.getUserReservations("")
            val reservation = userReservations.find { it.reservationId == reservationId }
            
            if (reservation != null) {
                Result.success(reservation)
            } else {
                Result.failure(Exception("Reservation not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
