package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.model.enums.EventStatus
import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(event: Event): Boolean {
        return try {
            // Get old event to check status change
            val oldEvent = eventRepository.getEventById(event.eventId)
            val wasCancelled = oldEvent?.status == EventStatus.CANCELLED
            val isNowCancelled = event.status == EventStatus.CANCELLED
            
            val success = eventRepository.updateEvent(event)
            
            // Send notification to participants when event is updated
            if (success) {
                try {
                    val reservationsFlow = reservationRepository.getEventReservations(event.eventId)
                    val reservations = reservationsFlow.first()
                    
                    // If event was just cancelled, send cancellation notification
                    if (isNowCancelled && !wasCancelled) {
                        reservations.forEach { reservation ->
                            sendNotificationUseCase(
                                userId = reservation.userId,
                                title = "Event Cancelled",
                                message = "The event '${event.name}' has been cancelled. Your reservation will be refunded within 3-5 business days.",
                                type = NotificationType.EVENT_UPDATE,
                                relatedEventId = event.eventId,
                                relatedReservationId = reservation.reservationId
                            )
                        }
                        // Also notify organizer
                        sendNotificationUseCase(
                            userId = event.organizerId,
                            title = "Event Cancelled",
                            message = "Your event '${event.name}' has been cancelled.",
                            type = NotificationType.EVENT_UPDATE,
                            relatedEventId = event.eventId
                        )
                    } else {
                        // Regular update notification
                        reservations.forEach { reservation ->
                            sendNotificationUseCase(
                                userId = reservation.userId,
                                title = "Event Updated",
                                message = "The event '${event.name}' has been updated. Please check the latest details.",
                                type = NotificationType.EVENT_UPDATE,
                                relatedEventId = event.eventId,
                                relatedReservationId = reservation.reservationId
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Don't fail event update if notification fails
                }
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
}