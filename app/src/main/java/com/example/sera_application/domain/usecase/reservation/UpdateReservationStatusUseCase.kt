package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class UpdateReservationStatusUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservationId: String, status: String): Boolean {
        if (reservationId.isBlank() || status.isBlank()) return false

        return try {
            val statusEnum = try {
                ReservationStatus.valueOf(status)
            } catch (e: IllegalArgumentException) {
                return false
            }
            val result = reservationRepository.updateReservationStatus(reservationId, statusEnum)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }
}
