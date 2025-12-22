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
        
        val result = reservationRepository.cancelReservation(reservationId)
        
        // Send notification on success
        result.fold(
            onSuccess = {
                reservation?.let { res ->
                    try {
                        val event = getEventByIdUseCase(res.eventId)
                        val eventName = event?.name ?: "Event"
                        sendNotificationUseCase(
                            userId = res.userId,
                            title = "Reservation Cancelled",
                            message = "Your reservation for $eventName has been cancelled. Refund will be processed within 3-5 business days.",
                            type = NotificationType.RESERVATION_UPDATE,
                            relatedEventId = res.eventId,
                            relatedReservationId = reservationId
                        )
                    } catch (e: Exception) {
                        // Don't fail cancellation if notification fails
                    }
                }
            },
            onFailure = { /* Reservation cancellation failed, no notification needed */ }
        )
        
        return result
    }
}