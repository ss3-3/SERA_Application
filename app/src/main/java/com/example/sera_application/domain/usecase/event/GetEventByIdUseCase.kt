package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Event? {
        if (eventId.isBlank()) return null

        return try {
            eventRepository.getEventById(eventId)
        } catch (e: Exception) {
            null
        }
    }
}