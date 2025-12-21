package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

class UpdateAvailableSeatsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(
        eventId: String,
        rockZoneDelta: Int,
        normalZoneDelta: Int
    ): Boolean {
        if (eventId.isBlank()) return false
        if (rockZoneDelta == 0 && normalZoneDelta == 0) return true
        
        return eventRepository.updateAvailableSeats(eventId, rockZoneDelta, normalZoneDelta)
    }
}
