package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

class ApproveEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Boolean {
        if (eventId.isBlank()) return false

        return try {
            eventRepository.approveEvent(eventId)
        } catch (e: Exception) {
            false
        }
    }
}