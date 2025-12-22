package com.example.sera_application.domain.usecase.payment

import android.util.Log
import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import javax.inject.Inject

/**
 * Use case for organizer to approve a refund request
 * Changes payment status from REFUND_PENDING to REFUNDED
 */
class ApproveRefundUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val sendNotificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(paymentId: String): Boolean {
        if (paymentId.isBlank()) {
            Log.e("ApproveRefundUseCase", "Payment ID is blank")
            return false
        }

        return try {
            // Get payment to verify it's in REFUND_PENDING status
            val payment = paymentRepository.getPaymentById(paymentId)
            if (payment == null) {
                Log.e("ApproveRefundUseCase", "Payment not found: $paymentId")
                return false
            }

            if (payment.status != PaymentStatus.REFUND_PENDING) {
                Log.w("ApproveRefundUseCase", "Payment ${paymentId} is not in REFUND_PENDING status. Current status: ${payment.status}")
                return false
            }

            // Approve the refund (change status to REFUNDED)
            val success = paymentRepository.approveRefund(paymentId)
            
            // Send notification to participant on success
            if (success) {
                try {
                    val event = getEventByIdUseCase(payment.eventId)
                    val eventName = event?.name ?: "Event"
                    sendNotificationUseCase(
                        userId = payment.userId,
                        title = "Refund Approved",
                        message = "Your refund request for $eventName has been approved. The refund of RM ${String.format("%.2f", payment.amount)} will be processed within 3-5 business days.",
                        type = NotificationType.PAYMENT_UPDATE,
                        relatedEventId = payment.eventId,
                        relatedReservationId = payment.reservationId
                    )
                } catch (e: Exception) {
                    Log.e("ApproveRefundUseCase", "Failed to send notification: ${e.message}", e)
                    // Don't fail approval if notification fails
                }
            }
            
            success
        } catch (e: Exception) {
            Log.e("ApproveRefundUseCase", "Error approving refund: ${e.message}", e)
            false
        }
    }
}

