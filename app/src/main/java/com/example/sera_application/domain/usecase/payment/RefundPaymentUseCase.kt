package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

class RefundPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(paymentId: String): Boolean {
        if (paymentId.isBlank()) return false

        return try {
            paymentRepository.refundPayment(paymentId)
        } catch (e: Exception) {
            false
        }
    }
}