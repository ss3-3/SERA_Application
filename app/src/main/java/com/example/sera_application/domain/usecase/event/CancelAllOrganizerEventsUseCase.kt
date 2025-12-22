package com.example.sera_application.domain.usecase.event

import android.util.Log
import com.example.sera_application.domain.model.enums.EventStatus
import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for cancelling all events created by an organizer.
 * Used when organizer account is suspended or deleted.
 * 
 * Uses UpdateEventUseCase to ensure proper notifications are sent to participants.
 */
class CancelAllOrganizerEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val updateEventUseCase: UpdateEventUseCase
) {
    suspend operator fun invoke(organizerId: String): Boolean {
        if (organizerId.isBlank()) return false

        return try {
            // Get all events created by this organizer
            val events = eventRepository.getEventsByOrganizer(organizerId)
            
            if (events.isEmpty()) {
                Log.d("CancelAllOrganizerEventsUseCase", "No events found for organizer: $organizerId")
                return true // No events to cancel, consider it successful
            }

            Log.d("CancelAllOrganizerEventsUseCase", "Cancelling ${events.size} events for organizer: $organizerId")

            // Cancel each event that is not already cancelled or completed
            var successCount = 0
            events.forEach { event ->
                // Only cancel events that are not already cancelled or completed
                if (event.status != EventStatus.CANCELLED && event.status != EventStatus.COMPLETED) {
                    try {
                        // Use UpdateEventUseCase to cancel event (this will handle notifications)
                        val cancelledEvent = event.copy(status = EventStatus.CANCELLED)
                        val updateSuccess = updateEventUseCase(cancelledEvent)
                        
                        if (updateSuccess) {
                            successCount++
                            Log.d("CancelAllOrganizerEventsUseCase", "Cancelled event: ${event.eventId} - ${event.name}")
                        } else {
                            Log.w("CancelAllOrganizerEventsUseCase", "Failed to cancel event: ${event.eventId} - ${event.name}")
                        }
                    } catch (e: Exception) {
                        Log.e("CancelAllOrganizerEventsUseCase", "Error cancelling event ${event.eventId}: ${e.message}", e)
                        // Continue with other events even if one fails
                    }
                }
            }

            Log.d("CancelAllOrganizerEventsUseCase", "Successfully cancelled $successCount out of ${events.size} events for organizer: $organizerId")
            true // Return true if at least we attempted to cancel all events
        } catch (e: Exception) {
            Log.e("CancelAllOrganizerEventsUseCase", "Error cancelling events for organizer $organizerId: ${e.message}", e)
            false
        }
    }
}

