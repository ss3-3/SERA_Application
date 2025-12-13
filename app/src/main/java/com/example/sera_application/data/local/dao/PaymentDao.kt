package com.example.sera_application.data.local.dao

import androidx.room.*
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
}