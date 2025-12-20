package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.Event

interface EventRepository {

    suspend fun getOrganizerName(id: String) : String

    suspend fun createEvent(event: Event): Boolean

    suspend fun updateEvent(event: Event): Boolean

    suspend fun deleteEvent(eventId: String): Boolean

    suspend fun getEventList(): List<Event>

    suspend fun getEventById(eventId: String): Event?

    suspend fun getEventsByOrganizer(organizerId: String): List<Event>

    suspend fun approveEvent(eventId: String): Boolean

    suspend fun rejectEvent(eventId: String): Boolean

    suspend fun closeEvent(eventId: String): Boolean

    suspend fun syncEventsFromFirebase(): List<Event>

//    suspend fun hasVenueTimeConflict(event: Event): Boolean

    suspend fun getApprovedEvents(): List<Event>
}
