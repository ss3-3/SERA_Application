package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.repository.EventRepository
import jakarta.inject.Inject

class GetApprovedEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): List<Event> {
        return repository.getApprovedEvents()
    }
}