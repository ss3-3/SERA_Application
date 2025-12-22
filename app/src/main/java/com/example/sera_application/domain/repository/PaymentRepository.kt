package com.example.sera_application.domain.repository

import com.example.sera_application.data.local.EventRevenue
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.model.uimodel.PaymentStatistics
import com.example.sera_application.domain.usecase.payment.PayPalOrderCreationResult

interface PaymentRepository {

    suspend fun processPayment(payment: Payment): Boolean

    suspend fun createPayPalOrder(amount: Double, currency: String): Result<PayPalOrderCreationResult>

    suspend fun capturePayPalOrder(orderId: String): Result<Unit>

    suspend fun getPaymentById(paymentId: String): Payment?

    suspend fun getPaymentHistory(userId: String): List<Payment>

    suspend fun refundPayment(paymentId: String): Boolean

    suspend fun approveRefund(paymentId: String): Boolean

    suspend fun getPaymentByReservationId(reservationId: String): Payment?

    suspend fun getPaymentsByEvent(eventId: String): List<Payment>
    suspend fun getTotalRevenueByEvents(eventIds: List<String>): Double
    suspend fun getTotalRevenue(): Double
    suspend fun getAllPayments(): List<Payment>
    suspend fun getPaymentStatistics(): PaymentStatistics
    suspend fun getRevenueTrend(days: Int, startDate: Long): List<Double>
    suspend fun getTopRevenueEventIds(limit: Int): List<EventRevenue>
}