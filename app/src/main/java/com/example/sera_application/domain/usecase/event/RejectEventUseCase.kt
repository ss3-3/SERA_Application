package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

class RejectEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Boolean {
        if (eventId.isBlank()) return false

        return try {
            eventRepository.rejectEvent(eventId)
        } catch (e: Exception) {
            false
        }
    }
}