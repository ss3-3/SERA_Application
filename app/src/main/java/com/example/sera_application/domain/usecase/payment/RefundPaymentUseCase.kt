package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import javax.inject.Inject

class RefundPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(paymentId: String): Boolean {
        if (paymentId.isBlank()) return false

        // Get payment details before refunding
        val payment = paymentRepository.getPaymentById(paymentId)
        
        return try {
            val success = paymentRepository.refundPayment(paymentId)
            
            // Send notification on success
            if (success && payment != null) {
                try {
                    val event = getEventByIdUseCase(payment.eventId)
                    val eventName = event?.name ?: "Event"
                    sendNotificationUseCase(
                        userId = payment.userId,
                        title = "Payment Refunded",
                        message = "Your payment of RM ${String.format("%.2f", payment.amount)} for $eventName has been refunded. The refund will be processed within 3-5 business days.",
                        type = NotificationType.PAYMENT_UPDATE,
                        relatedEventId = payment.eventId,
                        relatedReservationId = payment.reservationId
                    )
                } catch (e: Exception) {
                    // Don't fail refund if notification fails
                }
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
}