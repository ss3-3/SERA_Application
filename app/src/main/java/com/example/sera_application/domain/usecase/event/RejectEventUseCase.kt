package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import javax.inject.Inject

class RejectEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(eventId: String): Boolean {
        if (eventId.isBlank()) return false

        return try {
            val success = eventRepository.rejectEvent(eventId)
            
            // Send notification to organizer if rejection was successful
            if (success) {
                try {
                    val event = eventRepository.getEventById(eventId)
                    event?.let {
                        sendNotificationUseCase(
                            userId = it.organizerId,
                            title = "Event Rejected",
                            message = "Your event '${it.name}' has been rejected. Please review the event details and resubmit.",
                            type = NotificationType.EVENT_UPDATE,
                            relatedEventId = eventId
                        ).fold(
                            onSuccess = { /* Notification sent successfully */ },
                            onFailure = { /* Don't fail rejection if notification fails */ }
                        )
                    }
                } catch (e: Exception) {
                    // Don't fail rejection if notification fails
                }
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
}