package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class GetEventReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(eventId: String): Result<List<EventReservation>> {
        return try {
            if (eventId.isBlank()) {
                return Result.failure(Exception("Event ID cannot be empty"))
            }

            val reservations = reservationRepository.getEventReservations(eventId)
            Result.success(reservations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
