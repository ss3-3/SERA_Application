package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

class GetEventsByOrganizerUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(organizerId: String): List<Event> {
        if (organizerId.isBlank()) return emptyList()

        return try {
            eventRepository.getEventsByOrganizer(organizerId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}