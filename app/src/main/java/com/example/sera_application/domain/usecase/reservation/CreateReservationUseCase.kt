package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class CreateReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(reservation: EventReservation): String? {
        // Validate seats
        if (reservation.seats <= 0) return null

        return reservationRepository.createReservation(reservation)
    }
}