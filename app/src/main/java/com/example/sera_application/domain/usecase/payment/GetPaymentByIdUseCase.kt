package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

class GetPaymentByIdUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(paymentId: String): Payment? {
        if (paymentId.isBlank()) return null

        return try {
            paymentRepository.getPaymentById(paymentId)
        } catch (e: Exception) {
            null
        }
    }
}