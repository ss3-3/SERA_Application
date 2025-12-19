package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class GetAllReservationsUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    suspend operator fun invoke(): List<EventReservation> {
        return repository.getAllReservations()
    }
}
