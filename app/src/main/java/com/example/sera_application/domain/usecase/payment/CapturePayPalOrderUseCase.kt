package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

class CapturePayPalOrderUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(orderId: String): Result<Unit> {
        if (orderId.isBlank()) {
            return Result.failure(Exception("Order ID cannot be empty"))
        }
        return paymentRepository.capturePayPalOrder(orderId)
    }
}

