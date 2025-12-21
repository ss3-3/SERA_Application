package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import javax.inject.Inject

class CreateReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(reservation: EventReservation): Result<String> {
        // Validate seats
        if (reservation.seats <= 0) {
            return Result.failure(IllegalArgumentException("Seats must be greater than 0"))
        }

        // Create reservation
        val result = reservationRepository.createReservation(reservation)
        
        // Send notification on success
        result.fold(
            onSuccess = { reservationId ->
                // Fetch event details for notification message
                val event = getEventByIdUseCase(reservation.eventId)
                val eventName = event?.name ?: "Event"
                
                // Send notification to the user who made the reservation
                sendNotificationUseCase(
                    userId = reservation.userId,
                    title = "Reservation Confirmed",
                    message = "Your reservation for $eventName has been confirmed. Reservation ID: $reservationId",
                    type = NotificationType.RESERVATION_UPDATE,
                    relatedEventId = reservation.eventId,
                    relatedReservationId = reservationId
                )
                // Note: Notification failure doesn't fail the reservation creation
            },
            onFailure = { /* Reservation failed, no notification needed */ }
        )
        
        return result
    }
}