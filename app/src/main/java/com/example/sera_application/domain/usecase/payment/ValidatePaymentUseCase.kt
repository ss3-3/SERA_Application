package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.model.Payment
import javax.inject.Inject

class ValidatePaymentUseCase @Inject constructor() {
    operator fun invoke(payment: Payment): Boolean {
        // Validate payment details
        if (payment.amount <= 0) return false
        if (payment.userId.isBlank()) return false
        if (payment.eventId.isBlank()) return false

        return true
    }
}