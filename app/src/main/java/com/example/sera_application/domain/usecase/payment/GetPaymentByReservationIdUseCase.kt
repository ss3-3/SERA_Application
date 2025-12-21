package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

class GetPaymentByReservationIdUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(reservationId: String): Payment? {
        return repository.getPaymentByReservationId(reservationId)
    }
}
