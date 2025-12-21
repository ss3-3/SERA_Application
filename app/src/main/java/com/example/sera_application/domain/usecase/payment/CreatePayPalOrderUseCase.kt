package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

data class PayPalOrderCreationResult(
    val orderId: String,
    val approvalUrl: String
)

class CreatePayPalOrderUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(
        amount: Double,
        currency: String
    ): Result<PayPalOrderCreationResult> {
        if (amount <= 0) {
            return Result.failure(Exception("Amount must be greater than zero"))
        }
        if (currency.isBlank()) {
            return Result.failure(Exception("Currency cannot be empty"))
        }
        return paymentRepository.createPayPalOrder(amount, currency)
    }
}

