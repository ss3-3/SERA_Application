package com.example.sera_application.data.repository

import com.example.sera_application.data.local.dao.PaymentDao
import com.example.sera_application.data.mapper.PaymentMapper
import com.example.sera_application.data.remote.datasource.PaymentRemoteDataSource
import com.example.sera_application.data.remote.paypal.repository.PayPalRepository
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

/**
 * Implementation of PaymentRepository.
 * Coordinates payment operations between remote datasources, local database, and domain layer.
 */
class PaymentRepositoryImpl @Inject constructor(
    private val paymentRemoteDataSource: PaymentRemoteDataSource,
    private val payPalRepository: PayPalRepository,
    private val paymentDao: PaymentDao,
    private val mapper: PaymentMapper
) : PaymentRepository {

    override suspend fun processPayment(payment: Payment): Boolean {
        return try {
            // Step 1: Create PayPal order via backend
            val orderResult = payPalRepository.createOrder(
                amount = payment.amount.toString(),
                currencyCode = "MYR" // Default currency
            )

            if (orderResult.isSuccess) {
                // Step 2: Payment order created, but not yet captured
                // For now, save as PENDING payment record
                // Actual capture happens after user approves in PayPal
                val pendingPayment = payment.copy(
                    paymentId = "",
                    status = PaymentStatus.PENDING
                )

                val paymentId = paymentRemoteDataSource.savePayment(pendingPayment)

                // Step 3: Cache locally
                val savedPayment = payment.copy(
                    paymentId = paymentId,
                    status = PaymentStatus.PENDING
                )
                paymentDao.insertPayment(mapper.toEntity(savedPayment))

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getPaymentById(paymentId: String): Payment? {
        return try {
            // Try remote first
            val remotePayment = paymentRemoteDataSource.getPaymentById(paymentId)
            if (remotePayment != null) {
                paymentDao.insertPayment(mapper.toEntity(remotePayment))
                remotePayment
            } else {
                // Fallback to local
                val localEntity = paymentDao.getPaymentById(paymentId)
                localEntity?.let { mapper.toDomain(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPaymentHistory(userId: String): List<Payment> {
        return try {
            val remotePayments = paymentRemoteDataSource.getPaymentsByUser(userId)

            // Cache locally
            if (remotePayments.isNotEmpty()) {
                paymentDao.insertPayments(mapper.toEntityList(remotePayments))
            }

            remotePayments
        } catch (e: Exception) {
            // Fallback to local cache
            val localEntities = paymentDao.getPaymentsByUser(userId)
            mapper.toDomainList(localEntities)
        }
    }

    override suspend fun refundPayment(paymentId: String): Boolean {
        return try {
            val payment = getPaymentById(paymentId)
            if (payment != null) {
                // Update payment status to REFUND_PENDING
                paymentRemoteDataSource.updatePaymentStatus(
                    paymentId = paymentId,
                    status = PaymentStatus.REFUND_PENDING.name
                )

                // Update local cache
                paymentDao.updatePaymentStatus(paymentId, PaymentStatus.REFUND_PENDING.name)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getPaymentByReservationId(reservationId: String): Payment? {
        return try {
            paymentRemoteDataSource.getPaymentByReservation(reservationId)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createPayPalOrder(amount: Double, currency: String): Result<com.example.sera_application.domain.usecase.payment.PayPalOrderCreationResult> {
        return try {
            val orderResult = payPalRepository.createOrder(
                amount = amount.toString(),
                currencyCode = currency
            )
            if (orderResult.isSuccess) {
                val orderResponse = orderResult.getOrNull()
                val orderId = orderResponse?.id ?: ""
                // Extract approval URL from links (rel="approve")
                val approvalUrl = orderResponse?.links?.firstOrNull { it.rel == "approve" }?.href ?: ""
                Result.success(
                    com.example.sera_application.domain.usecase.payment.PayPalOrderCreationResult(
                        orderId = orderId,
                        approvalUrl = approvalUrl
                    )
                )
            } else {
                Result.failure(orderResult.exceptionOrNull() ?: Exception("Failed to create PayPal order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun capturePayPalOrder(orderId: String): Result<Unit> {
        return try {
            val result = payPalRepository.captureOrder(orderId)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to capture PayPal order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}