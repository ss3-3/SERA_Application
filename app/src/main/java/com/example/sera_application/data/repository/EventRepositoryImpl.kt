package com.example.sera_application.data.repository

import com.example.sera_application.data.local.dao.EventDao
import com.example.sera_application.data.local.entity.EventEntity
import com.example.sera_application.data.mapper.EventMapper
import com.example.sera_application.data.remote.datasource.EventRemoteDataSource
import com.example.sera_application.data.remote.datasource.UserRemoteDataSource
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventStatus
import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Implementation of EventRepository.
 * Coordinates event operations between remote datasource, local database, and domain layer.
 */
class EventRepositoryImpl @Inject constructor(
    private val remoteDataSource: EventRemoteDataSource,
    private val eventDao: EventDao,
    private val mapper: EventMapper,
    private val userRemoteDataSource: UserRemoteDataSource
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
            val eventId = remoteDataSource.createEvent(event)

            // Cache locally if event was created successfully
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

    override suspend fun approveEvent(eventId: String): Boolean {
        return try {
            remoteDataSource.updateEventStatus(eventId, EventStatus.APPROVED.name)
            eventDao.updateEventStatus(eventId, EventStatus.APPROVED.name)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun rejectEvent(eventId: String): Boolean {
        return try {
            remoteDataSource.updateEventStatus(eventId, EventStatus.REJECTED.name)
            eventDao.updateEventStatus(eventId, EventStatus.REJECTED.name)
            true
        } catch (e: Exception) {
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

    override suspend fun getPublicEvents(): List<Event> {
        return try {
            val events = remoteDataSource.getPublicEvents()

            // cache locally (optional)
            if (events.isNotEmpty()) {
                eventDao.insertEvents(events.map { mapper.toEntity(it) })
            }

            preloadOrganizerNamesForEvents(events)
            events
        } catch (e: Exception) {
            emptyList()
        }
    }

}