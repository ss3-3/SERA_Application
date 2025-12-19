package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class GetReservationByIdUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservationId: String): EventReservation? {
        if (reservationId.isBlank()) return null
        return reservationRepository.getReservationById(reservationId)
    }
}
