package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CloseEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(eventId: String): Boolean {
        if (eventId.isBlank()) return false

        return try {
            val success = eventRepository.closeEvent(eventId)
            
            // Send notification to organizer and participants
            if (success) {
                try {
                    val event = eventRepository.getEventById(eventId)
                    event?.let {
                        // Notify organizer
                        sendNotificationUseCase(
                            userId = it.organizerId,
                            title = "Event Completed",
                            message = "Your event '${it.name}' has been marked as completed.",
                            type = NotificationType.EVENT_UPDATE,
                            relatedEventId = eventId
                        )
                        
                        // Notify all participants who have reservations
                        val reservationsFlow = reservationRepository.getEventReservations(eventId)
                        val reservations = reservationsFlow.first()
                        reservations.forEach { reservation ->
                            sendNotificationUseCase(
                                userId = reservation.userId,
                                title = "Event Completed",
                                message = "The event '${it.name}' has been completed. Thank you for participating!",
                                type = NotificationType.EVENT_UPDATE,
                                relatedEventId = eventId,
                                relatedReservationId = reservation.reservationId
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Don't fail event closing if notification fails
                }
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
}