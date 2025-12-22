package com.example.sera_application.data.repository


import com.example.sera_application.data.local.EventRevenue
import com.example.sera_application.data.local.dao.PaymentDao
import com.example.sera_application.data.mapper.PaymentMapper
import com.example.sera_application.data.remote.datasource.PaymentRemoteDataSource
import com.example.sera_application.data.remote.paypal.repository.PayPalRepository
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.model.uimodel.PaymentStatistics
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.usecase.report.buildPaymentStatistics
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
        android.util.Log.d("PaymentRepositoryImpl", "=== Getting payment history for userId: '$userId' ===")
        return try {
            val remotePayments = paymentRemoteDataSource.getPaymentsByUser(userId)
            android.util.Log.d("PaymentRepositoryImpl", "✅ Got ${remotePayments.size} payments from remote for userId: '$userId'")


            // Cache locally
            if (remotePayments.isNotEmpty()) {
                paymentDao.insertPayments(mapper.toEntityList(remotePayments))
            }

            remotePayments
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "❌ Error getting payment history from remote: ${e.message}", e)
            // Fallback to local cache
            val localEntities = paymentDao.getPaymentsByUser(userId)
            android.util.Log.d("PaymentRepositoryImpl", "📦 Got ${localEntities.size} payments from local cache (fallback)")
            mapper.toDomainList(localEntities)
        }
    }

    override suspend fun refundPayment(paymentId: String): Boolean {
        return try {
            val payment = getPaymentById(paymentId)
            if (payment != null) {
                // Update payment status to REFUND_PENDING (when participant requests refund)
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

    override suspend fun approveRefund(paymentId: String): Boolean {
        return try {
            val payment = getPaymentById(paymentId)
            if (payment != null && payment.status == PaymentStatus.REFUND_PENDING) {
                // Update payment status to REFUNDED (when organizer approves refund)
                paymentRemoteDataSource.updatePaymentStatus(
                    paymentId = paymentId,
                    status = PaymentStatus.REFUNDED.name
                )

                // Update local cache
                paymentDao.updatePaymentStatus(paymentId, PaymentStatus.REFUNDED.name)
                true
            } else {
                android.util.Log.w("PaymentRepositoryImpl", "Cannot approve refund: payment not found or not in REFUND_PENDING status")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error approving refund: ${e.message}", e)
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

    override suspend fun getPaymentsByEvent(eventId: String): List<Payment> {
        return try {
            android.util.Log.d("PaymentRepositoryImpl", "Getting payments for eventId: $eventId")

            if (eventId.isBlank()) {
                android.util.Log.w("PaymentRepositoryImpl", "EventId is blank, returning empty list")
                return emptyList()
            }

            val remotePayments = paymentRemoteDataSource.getPaymentsByEvent(eventId)
            android.util.Log.d("PaymentRepositoryImpl", "Got ${remotePayments.size} payments from remote (Firebase) for eventId: $eventId")

            // Verify all payments have matching eventId
            val mismatched = remotePayments.filter { it.eventId != eventId }
            if (mismatched.isNotEmpty()) {
                android.util.Log.w("PaymentRepositoryImpl", "Found ${mismatched.size} payments with mismatched eventId")
                mismatched.forEach { payment ->
                    android.util.Log.w("PaymentRepositoryImpl", "Payment ${payment.paymentId} has eventId=${payment.eventId} but query was for eventId=$eventId")
                }
            }

            // Clear old cache for this event and update with fresh Firebase data
            try {
                // First, clear all cached payments for this event to ensure fresh data
                paymentDao.deletePaymentsByEvent(eventId)
                android.util.Log.d("PaymentRepositoryImpl", "Cleared old cache for eventId: $eventId")
                
                // Then update cache with fresh Firebase data
                if (remotePayments.isNotEmpty()) {
                    paymentDao.insertPayments(mapper.toEntityList(remotePayments))
                    android.util.Log.d("PaymentRepositoryImpl", "Cached ${remotePayments.size} fresh payments from Firebase for eventId: $eventId")
                } else {
                    android.util.Log.d("PaymentRepositoryImpl", "No payments in Firebase for eventId: $eventId, cache cleared")
                }
            } catch (e: Exception) {
                android.util.Log.e("PaymentRepositoryImpl", "Error updating cache: ${e.message}", e)
                // Continue even if cache update fails - we still return fresh Firebase data
            }

            // Always return fresh data from Firebase, not from cache
            android.util.Log.d("PaymentRepositoryImpl", "Returning ${remotePayments.size} payments from Firebase for eventId: $eventId")
            remotePayments
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error getting payments from remote for eventId $eventId: ${e.message}", e)
            e.printStackTrace()
            // Fallback to local cache
            try {
                val localEntities = paymentDao.getPaymentsByEvent(eventId)
                android.util.Log.d("PaymentRepositoryImpl", "Got ${localEntities.size} payments from local cache for eventId: $eventId")
                mapper.toDomainList(localEntities)
            } catch (localError: Exception) {
                android.util.Log.e("PaymentRepositoryImpl", "Error getting payments from local cache: ${localError.message}", localError)
                emptyList()
            }
        }
    }

    override suspend fun getTotalRevenueByEvents(eventIds: List<String>): Double {
        return try {
            // First, fetch payments for these events from Firebase to ensure local DB is synced
            val allPayments = mutableListOf<Payment>()
            eventIds.forEach { eventId ->
                try {
                    val eventPayments = paymentRemoteDataSource.getPaymentsByEvent(eventId)
                    allPayments.addAll(eventPayments)
                } catch (e: Exception) {
                    android.util.Log.e("PaymentRepositoryImpl", "Error fetching payments for event $eventId: ${e.message}", e)
                }
            }
            
            // Sync to local database
            if (allPayments.isNotEmpty()) {
                paymentDao.insertPayments(mapper.toEntityList(allPayments))
                android.util.Log.d("PaymentRepositoryImpl", "Synced ${allPayments.size} payments to local DB for ${eventIds.size} events")
            }
            
            // Return revenue from local DB (now synced with Firebase)
            paymentDao.getTotalRevenueByEvents(eventIds) ?: 0.0
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error getting total revenue by events: ${e.message}", e)
            // Fallback to local count if Firebase fetch fails
            try {
                paymentDao.getTotalRevenueByEvents(eventIds) ?: 0.0
            } catch (localException: Exception) {
                0.0
            }
        }
    }

    override suspend fun getTotalRevenue(): Double {
        return try {
            paymentDao.getTotalRevenue() ?: 0.0
        } catch (e: Exception) {
            0.0
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

    override suspend fun getAllPayments(): List<Payment> {
        return try {
            val localEntities = paymentDao.getAllPayments()
            mapper.toDomainList(localEntities)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPaymentStatistics(): PaymentStatistics {
        return try {
            val payments = getAllPayments()
            buildPaymentStatistics(payments)
        } catch (e: Exception) {
            PaymentStatistics(
                totalPayments = 0,
                successCount = 0,
                pendingCount = 0,
                failedCount = 0,
                totalRevenue = 0.0,
                successRate = 0.0,
                pendingRate = 0.0,
                failedRate = 0.0
            )
        }
    }

    override suspend fun getRevenueTrend(days: Int, startDate: Long): List<Double> {
        return try {
            // First, fetch payments from Firebase for the date range to ensure local DB is synced
            val endDate = System.currentTimeMillis()
            val remotePayments = paymentRemoteDataSource.getPaymentsByDateRange(startDate, endDate)
            
            android.util.Log.d("PaymentRepositoryImpl", "Fetched ${remotePayments.size} payments from Firebase for revenue trend (days=$days, startDate=$startDate)")
            
            // Sync to local database
            if (remotePayments.isNotEmpty()) {
                paymentDao.insertPayments(mapper.toEntityList(remotePayments))
                android.util.Log.d("PaymentRepositoryImpl", "Synced ${remotePayments.size} payments to local DB")
            }
            
            // Query local database for revenue trend (now synced with Firebase)
            val trendEntities = paymentDao.getRevenueTrend(startDate)
            
            android.util.Log.d("PaymentRepositoryImpl", "Revenue trend query returned ${trendEntities.size} data points")
            
            trendEntities.map { it.revenue }
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error getting revenue trend: ${e.message}", e)
            // Fallback to local count if Firebase fetch fails
            try {
                val trendEntities = paymentDao.getRevenueTrend(startDate)
                trendEntities.map { it.revenue }
            } catch (localException: Exception) {
                android.util.Log.e("PaymentRepositoryImpl", "Error getting revenue trend from local DB: ${localException.message}", localException)
                emptyList()
            }
        }
    }

    override suspend fun getTopRevenueEventIds(limit: Int): List<EventRevenue> {
        return try {
            paymentDao.getTopRevenueEvents(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updatePaymentStatus(paymentId: String, status: String): Boolean {
        return try {
            // Update remote
            paymentRemoteDataSource.updatePaymentStatus(paymentId, status)
            
            // Update local cache
            paymentDao.updatePaymentStatus(paymentId, status)
            true
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error updating payment status: ${e.message}", e)
            false
        }
    }
}