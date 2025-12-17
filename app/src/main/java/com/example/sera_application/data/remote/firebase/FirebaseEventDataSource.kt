package com.example.sera_application.data.remote.firebase

import com.example.sera_application.data.remote.datasource.EventRemoteDataSource
import com.example.sera_application.domain.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseEventDataSource(
    private val firestore: FirebaseFirestore
) : EventRemoteDataSource {

    private val eventsRef = firestore.collection("events")

    override suspend fun createEvent(event: Event): String {
        val docRef = if (event.eventId.isBlank()) {
            eventsRef.document()
        } else {
            eventsRef.document(event.eventId)
        }
        val eventWithId = event.copy(eventId = docRef.id)
        docRef.set(eventWithId).await()
        return docRef.id
    }

    override suspend fun updateEvent(event: Event) {
        eventsRef.document(event.eventId)
            .set(event)
            .await()
    }

    override suspend fun deleteEvent(eventId: String) {
        eventsRef.document(eventId).delete().await()
    }

    override suspend fun getEventList(): List<Event> {
        val snapshot = eventsRef.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
    }

    override suspend fun getEventById(eventId: String): Event? {
        val document = eventsRef.document(eventId).get().await()
        return if (document.exists()) {
            document.toObject(Event::class.java)
        } else {
            null
        }
    }

    override suspend fun getEventsByOrganizer(organizerId: String): List<Event> {
        val snapshot = eventsRef
            .whereEqualTo("organizerId", organizerId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
    }

    override suspend fun updateEventStatus(eventId: String, status: String) {
        eventsRef.document(eventId)
            .update("status", status)
            .await()
    }
}
