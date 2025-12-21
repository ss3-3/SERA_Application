package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetUserReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    operator fun invoke(userId: String): Flow<List<EventReservation>> {
        if (userId.isBlank()) return flowOf(emptyList())

        return reservationRepository.getUserReservations(userId)
    }
}