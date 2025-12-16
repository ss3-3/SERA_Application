package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

class GetPaymentHistoryUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(userId: String): List<Payment> {
        if (userId.isBlank()) return emptyList()

        return try {
            paymentRepository.getPaymentHistory(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}