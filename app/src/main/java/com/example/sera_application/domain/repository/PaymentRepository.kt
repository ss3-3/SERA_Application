package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.usecase.payment.PayPalOrderCreationResult

interface PaymentRepository {

    suspend fun processPayment(payment: Payment): Boolean

    suspend fun createPayPalOrder(amount: Double, currency: String): Result<PayPalOrderCreationResult>

    suspend fun capturePayPalOrder(orderId: String): Result<Unit>

    suspend fun getPaymentById(paymentId: String): Payment?

    suspend fun getPaymentHistory(userId: String): List<Payment>

    suspend fun refundPayment(paymentId: String): Boolean
}