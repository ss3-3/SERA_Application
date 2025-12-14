package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.Payment

interface PaymentRepository {

    suspend fun processPayment(payment: Payment): Boolean

    suspend fun getPaymentById(paymentId: String): Payment?

    suspend fun getPaymentHistory(userId: String): List<Payment>

    suspend fun refundPayment(paymentId: String): Boolean
}