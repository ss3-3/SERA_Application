package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class GetUserReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(userId: String): List<EventReservation> {
        if (userId.isBlank()) return emptyList()

        return try {
            reservationRepository.getUserReservations(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}