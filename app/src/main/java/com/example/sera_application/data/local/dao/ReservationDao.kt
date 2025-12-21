package com.example.sera_application.data.local.dao

import androidx.room.*
import com.example.sera_application.data.local.UserParticipation
import com.example.sera_application.data.local.entity.ReservationEntity
import com.example.sera_application.data.local.entity.UserEntity

@Dao
interface ReservationDao {

    data class TrendData(val period: Int, val count: Int)

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

    @Query("SELECT * FROM reservations ORDER BY createdAt DESC")
    suspend fun getAllReservations(): List<ReservationEntity>

    // Add
    @Query("SELECT COUNT(*) FROM reservations")
    suspend fun getTotalReservationCount(): Int

    // Add
    @Query("""
        SELECT COUNT(*) FROM reservations 
        WHERE eventId IN (:eventIds) AND status = 'SUCCESS'
    """)
    suspend fun getTotalParticipantsByEvents(eventIds: List<String>): Int

    // Add
    @Query("""
        SELECT COUNT(*) FROM reservations 
        WHERE eventId = :eventId AND status = 'SUCCESS'
    """)
    suspend fun getParticipantsByEvent(eventId: String): Int

    // Add
    @Query("SELECT COUNT(DISTINCT userId) FROM reservations")
    suspend fun getUniqueParticipantsCount(): Int

    // Add
    @Query("""
        SELECT userId, COUNT(DISTINCT eventId) as eventCount
        FROM reservations
        WHERE status = 'SUCCESS'
        GROUP BY userId
        ORDER BY eventCount DESC
        LIMIT :limit
    """)
    suspend fun getTopParticipants(limit: Int): List<UserParticipation>

    // Add
    @Query("""
        SELECT 
            CAST(strftime('%d', datetime(createdAt/1000, 'unixepoch')) AS INTEGER) / 5 * 5 as period,
            COUNT(*) as count
        FROM reservations
        WHERE createdAt >= :startDate
        GROUP BY period
        ORDER BY period
    """)
    suspend fun getMonthlyReservationTrend(startDate: Long): List<TrendData>

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