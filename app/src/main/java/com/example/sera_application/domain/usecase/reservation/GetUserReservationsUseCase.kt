package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class GetUserReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(userId: String): Result<List<EventReservation>> {
        return try {
            val reservations = reservationRepository.getUserReservations(userId)
            Result.success(reservations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}