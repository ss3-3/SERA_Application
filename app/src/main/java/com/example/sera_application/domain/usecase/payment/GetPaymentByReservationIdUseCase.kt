package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

class GetPaymentByReservationIdUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(reservationId: String): Payment? {
        android.util.Log.d("GetPaymentByReservation", "Looking for payment with reservationId: $reservationId")
        val payment = repository.getPaymentByReservationId(reservationId)
        if (payment != null) {
            android.util.Log.d("GetPaymentByReservation", "✅ Found payment: ${payment.paymentId}")
        } else {
            android.util.Log.w("GetPaymentByReservation", "⚠️ No payment found for reservationId: $reservationId")
        }
        return payment
    }
}
