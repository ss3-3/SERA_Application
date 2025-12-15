package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class CancelReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservationId: String): Boolean {
        if (reservationId.isBlank()) return false

        return try {
            reservationRepository.cancelReservation(reservationId)
        } catch (e: Exception) {
            false
        }
    }
}