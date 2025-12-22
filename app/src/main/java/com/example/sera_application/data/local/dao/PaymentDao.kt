package com.example.sera_application.data.local.dao

import androidx.room.*
import com.example.sera_application.data.local.EventRevenue
import com.example.sera_application.data.local.entity.PaymentEntity

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE paymentId = :paymentId")
    suspend fun getPaymentById(paymentId: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getPaymentsByUser(userId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE eventId = :eventId ORDER BY createdAt DESC")
    suspend fun getPaymentsByEvent(eventId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE reservationId = :reservationId")
    suspend fun getPaymentByReservation(reservationId: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getPaymentsByStatus(status: String): List<PaymentEntity>

    @Query("SELECT SUM(amount) FROM payments WHERE userId = :userId AND status = :status")
    suspend fun getTotalAmountByUserAndStatus(userId: String, status: String): Double?

    @Query("SELECT SUM(amount) FROM payments WHERE eventId = :eventId AND status = :status")
    suspend fun getTotalAmountByEventAndStatus(eventId: String, status: String): Double?

    // Add
    @Query("SELECT * FROM payments")
    suspend fun getAllPayments(): List<PaymentEntity>

    // Add
    @Query("""
        SELECT SUM(amount) FROM payments 
        WHERE eventId IN (:eventIds) AND status = 'SUCCESS'
    """)
    suspend fun getTotalRevenueByEvents(eventIds: List<String>): Double

    // Add
    @Query("""
        SELECT SUM(amount) FROM payments 
        WHERE createdAt BETWEEN :startDate AND :endDate AND status = 'SUCCESS'
    """)
    suspend fun getRevenueByDateRange(startDate: Long, endDate: Long): Double

    // Add
    @Query("""
        SELECT 
            date(createdAt/1000, 'unixepoch') as date,
            IFNULL(SUM(amount), 0) as revenue
        FROM payments
        WHERE createdAt >= :startDate AND status = 'SUCCESS'
        GROUP BY date
        ORDER BY date
        LIMIT :days
    """)
    suspend fun getRevenueTrend(days: Int, startDate: Long): List<RevenueTrendEntity>

    // Add
    @Query("""
        SELECT eventId, SUM(amount) as revenue
        FROM payments
        WHERE status = 'SUCCESS'
        GROUP BY eventId
        ORDER BY revenue DESC
        LIMIT :limit
    """)
    suspend fun getTopRevenueEvents(limit: Int): List<EventRevenue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE paymentId = :paymentId")
    suspend fun deletePaymentById(paymentId: String)

    @Query("UPDATE payments SET status = :status WHERE paymentId = :paymentId")
    suspend fun updatePaymentStatus(paymentId: String, status: String)

    @Query("DELETE FROM payments")
    suspend fun clearAllPayments()

    @Query("DELETE FROM payments WHERE eventId = :eventId")
    suspend fun deletePaymentsByEvent(eventId: String)

    // Add for PaymentStatistics
    @Query("SELECT SUM(amount) FROM payments WHERE status = 'SUCCESS'")
    suspend fun getTotalRevenue(): Double

    @Query("SELECT COUNT(*) FROM payments WHERE status = :status")
    suspend fun getPaymentCountByStatus(status: String): Int
}

data class RevenueTrendEntity(
    val date: String,
    val revenue: Double
)