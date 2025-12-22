package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import javax.inject.Inject

class CancelReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(reservationId: String): Result<Unit> {
        if (reservationId.isBlank()) {
            return Result.failure(IllegalArgumentException("Reservation ID cannot be blank"))
        }

        // Get reservation details before cancelling
        val reservation = reservationRepository.getReservationById(reservationId)
            ?: return Result.failure(IllegalArgumentException("Reservation not found"))
        
        // Get event details to check event date/time
        val event = getEventByIdUseCase(reservation.eventId)
            ?: return Result.failure(IllegalArgumentException("Event not found"))
        
        // Validate: Cannot cancel within 24 hours before event starts
        val currentTime = System.currentTimeMillis()
        val eventStartTime = event.date // Event date in milliseconds
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000L
        val timeDifference = eventStartTime - currentTime
        
        if (timeDifference < twentyFourHoursInMillis) {
            return Result.failure(
                IllegalStateException(
                    "Cancellation not allowed. You cannot cancel a reservation within 24 hours before the event starts."
                )
            )
        }
        
        val result = reservationRepository.cancelReservation(reservationId)
        
        // Send notification on success
        result.fold(
            onSuccess = {
                try {
                    val eventName = event.name
                    sendNotificationUseCase(
                        userId = reservation.userId,
                        title = "Reservation Cancelled",
                        message = "Your reservation for $eventName has been cancelled. Refund will be processed within 3-5 business days.",
                        type = NotificationType.RESERVATION_UPDATE,
                        relatedEventId = reservation.eventId,
                        relatedReservationId = reservationId
                    )
                } catch (e: Exception) {
                    // Don't fail cancellation if notification fails
                }
            },
            onFailure = { /* Reservation cancellation failed, no notification needed */ }
        )
        
        return result
    }
}