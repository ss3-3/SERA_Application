package com.example.sera_application.data.local.dao

import androidx.room.*
import com.example.sera_application.data.local.entity.ReservationEntity

@Dao
interface ReservationDao {

    @Query("SELECT * FROM reservations WHERE reservationId = :reservationId")
    suspend fun getReservationById(reservationId: String): ReservationEntity?

    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getReservationsByUser(userId: String): List<ReservationEntity>

    @Query("SELECT * FROM reservations WHERE eventId = :eventId ORDER BY createdAt DESC")
    suspend fun getReservationsByEvent(eventId: String): List<ReservationEntity>

    @Query("SELECT * FROM reservations WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getReservationsByStatus(status: String): List<ReservationEntity>

    @Query("SELECT * FROM reservations WHERE userId = :userId AND eventId = :eventId")
    suspend fun getUserReservationForEvent(userId: String, eventId: String): ReservationEntity?

    @Query("SELECT SUM(seats) FROM reservations WHERE eventId = :eventId AND status IN (:statuses)")
    suspend fun getTotalSeatsForEvent(eventId: String, statuses: List<String>): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservations(reservations: List<ReservationEntity>)

    @Update
    suspend fun updateReservation(reservation: ReservationEntity)

    @Delete
    suspend fun deleteReservation(reservation: ReservationEntity)

    @Query("DELETE FROM reservations WHERE reservationId = :reservationId")
    suspend fun deleteReservationById(reservationId: String)

    @Query("UPDATE reservations SET status = :status WHERE reservationId = :reservationId")
    suspend fun updateReservationStatus(reservationId: String, status: String)

    @Query("DELETE FROM reservations")
    suspend fun clearAllReservations()
}