package com.example.sera_application.domain.usecase.payment

import android.util.Log
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

/**
 * Use case to set all payments for an event to REFUND_PENDING status.
 * Used when an event is cancelled.
 */
class SetEventPaymentsToRefundPendingUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(eventId: String): Boolean {
        if (eventId.isBlank()) {
            Log.w("SetEventPaymentsToRefundPendingUseCase", "EventId is blank")
            return false
        }

        return try {
            Log.d("SetEventPaymentsToRefundPendingUseCase", "Setting payments to REFUND_PENDING for event: $eventId")
            
            // Get all payments for this event
            val payments = paymentRepository.getPaymentsByEvent(eventId)
            
            if (payments.isEmpty()) {
                Log.d("SetEventPaymentsToRefundPendingUseCase", "No payments found for event: $eventId")
                return true // No payments to update, consider it successful
            }

            Log.d("SetEventPaymentsToRefundPendingUseCase", "Found ${payments.size} payments for event: $eventId")

            // Update each payment that is not already REFUNDED or REFUND_PENDING
            var successCount = 0
            payments.forEach { payment ->
                // Only update payments that are SUCCESS (paid) - don't update already refunded or pending refunds
                if (payment.status == PaymentStatus.SUCCESS) {
                    try {
                        val success = paymentRepository.updatePaymentStatus(
                            paymentId = payment.paymentId,
                            status = PaymentStatus.REFUND_PENDING.name
                        )
                        if (success) {
                            successCount++
                            Log.d("SetEventPaymentsToRefundPendingUseCase", "Updated payment ${payment.paymentId} to REFUND_PENDING")
                        } else {
                            Log.w("SetEventPaymentsToRefundPendingUseCase", "Failed to update payment ${payment.paymentId}")
                        }
                    } catch (e: Exception) {
                        Log.e("SetEventPaymentsToRefundPendingUseCase", "Error updating payment ${payment.paymentId}: ${e.message}", e)
                        // Continue with other payments even if one fails
                    }
                } else {
                    Log.d("SetEventPaymentsToRefundPendingUseCase", "Skipping payment ${payment.paymentId} with status: ${payment.status}")
                }
            }

            Log.d("SetEventPaymentsToRefundPendingUseCase", "Successfully updated $successCount out of ${payments.size} payments to REFUND_PENDING for event: $eventId")
            true // Return true if we attempted to update all payments
        } catch (e: Exception) {
            Log.e("SetEventPaymentsToRefundPendingUseCase", "Error setting payments to REFUND_PENDING for event $eventId: ${e.message}", e)
            false
        }
    }
}

