package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(event: Event): Boolean {
        return try {
            eventRepository.updateEvent(event)
        } catch (e: Exception) {
            false
        }
    }
}