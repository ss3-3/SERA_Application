package com.example.sera_application.data.remote.datasource

import com.example.sera_application.domain.model.Event

interface EventRemoteDataSource {
    
    suspend fun createEvent(event: Event): String // Returns eventId
    
    suspend fun updateEvent(event: Event)
    
    suspend fun deleteEvent(eventId: String)
    
    suspend fun getEventList(): List<Event>
    
    suspend fun getEventById(eventId: String): Event?
    
    suspend fun getEventsByOrganizer(organizerId: String): List<Event>
    
    suspend fun updateEventStatus(eventId: String, status: String)

    suspend fun getEventListFromFirebase(): List<Event>

    suspend fun getPublicEvents(): List<Event>
}