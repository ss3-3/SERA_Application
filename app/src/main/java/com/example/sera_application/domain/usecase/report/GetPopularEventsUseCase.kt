package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

class GetPopularEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(limit: Int = 3): List<Event> {
        if (limit <= 0) return emptyList()

        return try {
            eventRepository.getPopularEvents(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
}