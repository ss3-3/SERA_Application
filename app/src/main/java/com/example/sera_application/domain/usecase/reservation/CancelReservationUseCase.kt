package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class CancelReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservationId: String): Result<Boolean> {
        return try {
            val success = reservationRepository.cancelReservation(reservationId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to cancel reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}