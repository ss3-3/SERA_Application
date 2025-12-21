package com.example.sera_application.data.local.dao

import androidx.room.*
import com.example.sera_application.data.local.entity.EventEntity

@Dao
interface EventDao {

    @Query("SELECT * FROM events WHERE eventId = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    suspend fun getAllEvents(): List<EventEntity>

    // Add
    @Query("SELECT COUNT(*) FROM events")
    suspend fun getTotalEventCount(): Int

    @Query("SELECT * FROM events WHERE organizerId = :organizerId ORDER BY createdAt DESC")
    suspend fun getEventsByOrganizer(organizerId: String): List<EventEntity>

    @Query("SELECT * FROM events WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getEventsByStatus(status: String): List<EventEntity>

    @Query("SELECT * FROM events WHERE createdAt >= :fromTime AND createdAt <= :toTime ORDER BY createdAt ASC")
    suspend fun getEventsByDateRange(fromTime: Long, toTime: Long): List<EventEntity>

    // Add
    @Query("""
        SELECT e.* FROM events e
        INNER JOIN reservations r ON e.eventId = r.eventId
        GROUP BY e.eventId
        ORDER BY COUNT(r.reservationId) DESC
        LIMIT :limit
    """)
    suspend fun getPopularEvents(limit: Int): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE eventId = :eventId")
    suspend fun deleteEventById(eventId: String)

    @Query("UPDATE events SET status = :status WHERE eventId = :eventId")
    suspend fun updateEventStatus(eventId: String, status: String)

    @Query("DELETE FROM events")
    suspend fun clearAllEvents()

    @Transaction
    suspend fun clearAndInsertEvents(events: List<EventEntity>) {
        clearAllEvents()
        insertEvents(events)
    }

    @Query("SELECT * FROM events WHERE eventName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchEvents(query: String): List<EventEntity>

    @Query("""
    SELECT * FROM events
    WHERE location = :location
    AND date = :date
""")
    suspend fun getEventsAtSameVenueAndDate(
        location: String,
        date: Long
    ): List<EventEntity>
}