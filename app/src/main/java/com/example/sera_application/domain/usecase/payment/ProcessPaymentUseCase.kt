package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(payment: Payment): Boolean {
        // Validate payment amount
        if (payment.amount <= 0) return false

        return try {
            paymentRepository.processPayment(payment)
        } catch (e: Exception) {
            false
        }
    }
}