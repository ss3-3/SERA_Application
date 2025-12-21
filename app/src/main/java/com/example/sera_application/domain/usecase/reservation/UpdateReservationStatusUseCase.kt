package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class UpdateReservationStatusUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(
        reservationId: String,
        status: ReservationStatus
    ): Result<Unit> {
        if (reservationId.isBlank()) {
            return Result.failure(IllegalArgumentException("Reservation ID cannot be blank"))
        }

        return reservationRepository.updateReservationStatus(reservationId, status)
    }
}