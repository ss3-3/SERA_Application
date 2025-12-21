package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class UpdateReservationStatusUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservationId: String, status: String): Boolean {
        if (reservationId.isBlank() || status.isBlank()) return false

        return try {
            reservationRepository.updateReservationStatus(reservationId, status)
        } catch (e: Exception) {
            false
        }
    }
}