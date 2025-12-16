package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class CreateReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservation: EventReservation): Result<EventReservation> {
        return try {
            val success = reservationRepository.createReservation(reservation)
            if (success) {
                Result.success(reservation)
            } else {
                Result.failure(Exception("Failed to create reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}