package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetEventReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(eventId: String): Flow<List<EventReservation>> {
        if (eventId.isBlank()) return flowOf(emptyList())

        return reservationRepository.getEventReservations(eventId)
    }
}
