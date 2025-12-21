package com.example.sera_application.data.repository

import com.example.sera_application.data.local.dao.EventDao
import com.example.sera_application.data.local.dao.UserDao
import com.example.sera_application.data.local.entity.EventEntity
import com.example.sera_application.data.mapper.EventMapper
import com.example.sera_application.data.remote.datasource.EventRemoteDataSource
import com.example.sera_application.data.remote.datasource.UserRemoteDataSource
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventStatus
import com.example.sera_application.domain.repository.EventRepository
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Implementation of EventRepository.
 * Coordinates event operations between remote datasource, local database, and domain layer.
 */
class EventRepositoryImpl @Inject constructor(
    private val remoteDataSource: EventRemoteDataSource,
    private val eventDao: EventDao,
    private val mapper: EventMapper,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userDao: UserDao
) : EventRepository {

    /**
     * Simple in-memory cache for organizer names to avoid N+1 lookups.
     * Key: organizerId, Value: fullName
     */
    private val organizerNameCache: MutableMap<String, String> = mutableMapOf()

    /**
     * Preload organizer names for a list of events.
     *
     * Tries to fetch all users once and build a map for the required organizerIds.
     * Falls back to per-id lookups via getOrganizerName if that fails.
     */
    private suspend fun preloadOrganizerNamesForEvents(events: List<Event>) {
        val organizerIds = events
            .map { it.organizerId }
            .filter { it.isNotBlank() }
            .toSet()

        // Filter out ids that are already cached
        val missingIds = organizerIds.filterNot { organizerNameCache.containsKey(it) }
        if (missingIds.isEmpty()) return

        try {
            // Try to resolve in batch using all users
            val allUsers = userRemoteDataSource.getAllUsers()
            val resolvedMap = allUsers
                .filter { it.userId in missingIds }
                .associate { user -> user.userId to user.fullName }

            organizerNameCache.putAll(resolvedMap)
        } catch (e: Exception) {
            // Fallback: per-id lookup; getOrganizerName() will populate cache as it goes
            missingIds.forEach { id ->
                getOrganizerName(id)
            }
        }
    }

    override suspend fun getOrganizerName(organizerId: String): String {
        // Check in-memory cache first
        organizerNameCache[organizerId]?.let { return it }

        return try {
            val user = userRemoteDataSource.getUserProfile(organizerId)
            val name = user?.fullName ?: ""
            if (name.isNotBlank()) {
                organizerNameCache[organizerId] = name
            }
            name
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun createEvent(event: Event): Boolean {
        return try {

            // ---------- STEP 4B: CHECK DUPLICATE VENUE + TIME ----------
            val existingEvents = eventDao.getEventsAtSameVenueAndDate(
                location = event.location,
                date = event.date
            )

            val newStart = event.startTime
            val newEnd = event.endTime

            val clash = existingEvents.any { existing ->
                val existStart = existing.startTime
                val existEnd = existing.endTime

                // time overlap rule
                newStart < existEnd && newEnd > existStart
            }

            if (clash) {
                throw IllegalStateException(
                    "Another event already exists at this venue and time"
                )
            }
            // ----------------------------------------------------------

            val eventId = remoteDataSource.createEvent(event)

            val createdEvent = remoteDataSource.getEventById(eventId)
            createdEvent?.let {
                eventDao.insertEvent(mapper.toEntity(it))
            }

            eventId.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateEvent(event: Event): Boolean {
        return try {
            remoteDataSource.updateEvent(event)
            eventDao.insertEvent(mapper.toEntity(event))
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteEvent(eventId: String): Boolean {
        return try {
            remoteDataSource.deleteEvent(eventId)
            eventDao.deleteEventById(eventId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getEventList(): List<Event> {
        return try {
            val remoteEvents = remoteDataSource.getEventList()

            // Cache locally
            if (remoteEvents.isNotEmpty()) {
                val entities = remoteEvents.map { event ->
                    mapper.toEntity(event)
                }
                eventDao.insertEvents(entities)
            }

            // Preload organizer names for all events to avoid N+1 calls
            preloadOrganizerNamesForEvents(remoteEvents)
            val organizerNameSnapshot = organizerNameCache.toMap()

            remoteEvents.map { event ->
                // Ensure organizerName is populated
                if (event.organizerName.isBlank()) {
                    val organizerName = organizerNameSnapshot[event.organizerId] ?: ""
                    event.copy(organizerName = organizerName)
                } else {
                    event
                }
            }
        } catch (e: Exception) {
            // Fallback to local cache
            val localEntities = eventDao.getAllEvents()
            val organizerNameSnapshot = organizerNameCache.toMap()

            mapper.toDomainList(localEntities) { organizerId ->
                organizerNameSnapshot[organizerId] ?: ""
            }
        }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return try {
            // Try remote first
            val remoteEvent = remoteDataSource.getEventById(eventId)
            if (remoteEvent != null) {
                eventDao.insertEvent(mapper.toEntity(remoteEvent))
                // Ensure organizerName is populated
                if (remoteEvent.organizerName.isBlank()) {
                    remoteEvent.copy(organizerName = getOrganizerName(remoteEvent.organizerId))
                } else {
                    remoteEvent
                }
            } else {
                // Fallback to local
                val localEntity = eventDao.getEventById(eventId)
                localEntity?.let { entity ->
                    mapper.toDomain(entity, getOrganizerName(entity.organizerId))
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getEventsByOrganizer(organizerId: String): List<Event> {
        return try {
            val remoteEvents = remoteDataSource.getEventsByOrganizer(organizerId)

            // Cache locally
            if (remoteEvents.isNotEmpty()) {
                val entities = remoteEvents.map { event ->
                    mapper.toEntity(event)
                }
                eventDao.insertEvents(entities)
            }

            // Preload organizer names for these events
            preloadOrganizerNamesForEvents(remoteEvents)
            val organizerNameSnapshot = organizerNameCache.toMap()

            remoteEvents.map { event ->
                // Ensure organizerName is populated
                if (event.organizerName.isBlank()) {
                    val name = organizerNameSnapshot[event.organizerId] ?: ""
                    event.copy(organizerName = name)
                } else {
                    event
                }
            }
        } catch (e: Exception) {
            // Fallback to local cache
            val localEntities = eventDao.getEventsByOrganizer(organizerId)
            val organizerNameSnapshot = organizerNameCache.toMap()

            mapper.toDomainList(localEntities) { id ->
                if (id == organizerId) {
                    organizerNameSnapshot[id] ?: ""
                } else {
                    ""
                }
            }
        }
    }


    // Add
    override suspend fun getTotalEventCount(): Int {
        return eventDao.getTotalEventCount()
    }

    override suspend fun getAllEvents(): List<Event> {
        val entities = eventDao.getAllEvents()
        // Preload organizer names for all events
        val organizerIds = entities.map { it.organizerId }.distinct()
        organizerIds.forEach { id ->
            if (!organizerNameCache.containsKey(id)) {
                getOrganizerName(id)
            }
        }
        val organizerNameSnapshot = organizerNameCache.toMap()
        return mapper.toDomainList(entities) { organizerId ->
            organizerNameSnapshot[organizerId] ?: ""
        }
    }

    override suspend fun getPopularEvents(limit: Int): List<Event> {
        val entities = eventDao.getPopularEvents(limit)
        // Preload organizer names for these events
        val organizerIds = entities.map { it.organizerId }.distinct()
        organizerIds.forEach { id ->
            if (!organizerNameCache.containsKey(id)) {
                getOrganizerName(id)
            }
        }
        val organizerNameSnapshot = organizerNameCache.toMap()
        return mapper.toDomainList(entities) { organizerId ->
            organizerNameSnapshot[organizerId] ?: ""
        }
    }

    override suspend fun getEventsByDateRange(startDate: Long, endDate: Long): List<Event> {
        val entities = eventDao.getEventsByDateRange(startDate, endDate)
        // Preload organizer names for these events
        val organizerIds = entities.map { it.organizerId }.distinct()
        organizerIds.forEach { id ->
            if (!organizerNameCache.containsKey(id)) {
                getOrganizerName(id)
            }
        }
        val organizerNameSnapshot = organizerNameCache.toMap()
        return mapper.toDomainList(entities) { organizerId ->
            organizerNameSnapshot[organizerId] ?: ""
        }
    }


    override suspend fun approveEvent(eventId: String): Boolean {
        return try {
            android.util.Log.d("EventRepository", "Approving event: $eventId with status: ${EventStatus.APPROVED.name}")
            remoteDataSource.updateEventStatus(eventId, EventStatus.APPROVED.name)
            eventDao.updateEventStatus(eventId, EventStatus.APPROVED.name)
            android.util.Log.d("EventRepository", "Successfully approved event: $eventId")
            true
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Failed to approve event $eventId: ${e.message}", e)
            false
        }
    }

    override suspend fun rejectEvent(eventId: String): Boolean {
        return try {
            android.util.Log.d("EventRepository", "Rejecting event: $eventId with status: ${EventStatus.REJECTED.name}")
            remoteDataSource.updateEventStatus(eventId, EventStatus.REJECTED.name)
            eventDao.updateEventStatus(eventId, EventStatus.REJECTED.name)
            android.util.Log.d("EventRepository", "Successfully rejected event: $eventId")
            true
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Failed to reject event $eventId: ${e.message}", e)
            false
        }
    }

    override suspend fun closeEvent(eventId: String): Boolean {
        return try {
            remoteDataSource.updateEventStatus(eventId, EventStatus.COMPLETED.name)
            eventDao.updateEventStatus(eventId, EventStatus.COMPLETED.name)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun syncEventsFromFirebase(): List<Event> {
        val snapshot = remoteDataSource.getEventListFromFirebase()

        val entities = snapshot.map { event ->
            mapper.toEntity(event)
        }

        eventDao.clearAndInsertEvents(entities)

        return snapshot
    }

//    private fun isTimeOverlap(
//        start1: Long,
//        end1: Long,
//        start2: Long,
//        end2: Long
//    ): Boolean {
//        return start1 < end2 && start2 < end1
//    }
//
//    override suspend fun hasVenueTimeConflict(event: Event): Boolean {
//        val localEvents = eventDao.getEventsAtSameVenueAndDate(
//            location = event.location,
//            date = event.date
//        )
//
//        return localEvents.any { existing ->
//            isTimeOverlap(
//                event.startTime,
//                event.endTime,
//                existing.startTime,
//                existing.endTime
//            )
//        }
//    }

    override suspend fun getApprovedEvents(): List<Event> {
        val entities = eventDao.getEventsByStatus("APPROVED")
        // Preload organizer names for these events
        val organizerIds = entities.map { it.organizerId }.distinct()
        organizerIds.forEach { id ->
            if (!organizerNameCache.containsKey(id)) {
                getOrganizerName(id)
            }
        }
        val organizerNameSnapshot = organizerNameCache.toMap()
        return mapper.toDomainList(entities) { organizerId ->
            organizerNameSnapshot[organizerId] ?: ""
        }
    }

    override suspend fun updateAvailableSeats(
        eventId: String,
        rockZoneDelta: Int,
        normalZoneDelta: Int
    ): Boolean {
        return try {
            // Update remote
            remoteDataSource.updateAvailableSeats(eventId, rockZoneDelta, normalZoneDelta)

            // Update local
            val localEvent = eventDao.getEventById(eventId)
            localEvent?.let { entity ->
                val updatedEntity = entity.copy(
                    rockZoneSeats = entity.rockZoneSeats + rockZoneDelta,
                    normalZoneSeats = entity.normalZoneSeats + normalZoneDelta,
                    availableSeats = entity.availableSeats + rockZoneDelta + normalZoneDelta
                )
                eventDao.insertEvent(updatedEntity)
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("EventRepositoryImpl", "Error updating available seats for event $eventId", e)
            false
        }
    }

}