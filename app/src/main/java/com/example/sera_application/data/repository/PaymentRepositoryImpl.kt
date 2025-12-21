package com.example.sera_application.data.repository

import com.example.sera_application.data.local.dao.PaymentDao
import com.example.sera_application.data.mapper.PaymentMapper
import com.example.sera_application.data.remote.datasource.PaymentRemoteDataSource
import com.example.sera_application.data.remote.datasource.PayPalRemoteDataSource
import com.example.sera_application.data.remote.paypal.PayPalOrderResult
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.usecase.payment.PayPalOrderCreationResult
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val paymentRemoteDataSource: PaymentRemoteDataSource,
    private val payPalRemoteDataSource: PayPalRemoteDataSource,
    private val paymentDao: PaymentDao,
    private val mapper: PaymentMapper
) : PaymentRepository {

    override suspend fun processPayment(payment: Payment): Boolean {
        return try {
            val paymentId = paymentRemoteDataSource.savePayment(payment)
            val savedPayment = payment.copy(paymentId = paymentId)
            paymentDao.insertPayment(mapper.toEntity(savedPayment))
            true
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

    override suspend fun createPayPalOrder(
        amount: Double,
        currency: String
    ): Result<PayPalOrderCreationResult> {
        return try {
            val orderResult = payPalRemoteDataSource.createOrder(amount, currency)
            when (orderResult) {
                is PayPalOrderResult.Success -> {
                    Result.success(
                        PayPalOrderCreationResult(
                            orderId = orderResult.orderId,
                            approvalUrl = orderResult.approvalUrl
                        )
                    )
                }
                is PayPalOrderResult.Error -> {
                    Result.failure(Exception(orderResult.error))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun capturePayPalOrder(orderId: String): Result<Unit> {
        return try {
            val captureResult = payPalRemoteDataSource.captureOrder(orderId)
            when (captureResult) {
                is com.example.sera_application.data.remote.paypal.PayPalCaptureResult.Success -> {
                    Result.success(Unit)
                }
                is com.example.sera_application.data.remote.paypal.PayPalCaptureResult.Error -> {
                    Result.failure(Exception(captureResult.error))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
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
            if (payment != null && payment.status == PaymentStatus.SUCCESS) {
                paymentRemoteDataSource.updatePaymentStatus(
                    paymentId = paymentId,
                    status = PaymentStatus.REFUND_PENDING.name
                )

                paymentDao.updatePaymentStatus(paymentId, PaymentStatus.REFUND_PENDING.name)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
