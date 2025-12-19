package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class GetEventReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(eventId: String): List<EventReservation> {
        if (eventId.isBlank()) return emptyList()

        return try {
            reservationRepository.getEventReservations(eventId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
