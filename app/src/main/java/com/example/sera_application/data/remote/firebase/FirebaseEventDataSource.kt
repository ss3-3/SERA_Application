package com.example.sera_application.data.remote.firebase

import android.util.Log
import com.example.sera_application.data.remote.datasource.EventRemoteDataSource
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.domain.model.enums.EventStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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
        val eventMap = eventToFirestoreMap(eventWithId)
        docRef.set(eventMap).await()
        return docRef.id
    }

    override suspend fun updateEvent(event: Event) {
        val eventMap = eventToFirestoreMap(event)
        eventsRef.document(event.eventId).set(eventMap).await()
    }

    override suspend fun deleteEvent(eventId: String) {
        eventsRef.document(eventId).delete().await()
    }

    override suspend fun getEventList(): List<Event> {
        val snapshot = eventsRef.get().await()
        return snapshot.documents.mapNotNull { it.toEvent() }
    }

    // This function is required by the interface.
    override suspend fun getEventListFromFirebase(): List<Event> {
        val snapshot = eventsRef.get().await()
        return snapshot.documents.mapNotNull { it.toEvent() }
    }

    override suspend fun getEventById(eventId: String): Event? {
        val document = eventsRef.document(eventId).get().await()
        return if (document.exists()) document.toEvent() else null
    }

    override suspend fun getEventsByOrganizer(organizerId: String): List<Event> {
        val snapshot = eventsRef.whereEqualTo("organizerId", organizerId).get().await()
        return snapshot.documents.mapNotNull { it.toEvent() }
    }

    override suspend fun updateEventStatus(eventId: String, status: String) {
        eventsRef.document(eventId).update("status", status).await()
    }

    override suspend fun getPublicEvents(): List<Event> {
        val snapshot = firestore.collection("events")
            .whereEqualTo("status", EventStatus.APPROVED.name)
            .limit(10)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toEvent() }
    }

    private fun eventToFirestoreMap(event: Event): Map<String, Any?> {
        return mapOf(
            "eventId" to event.eventId,
            "eventName" to event.name,
            "organizerId" to event.organizerId,
            "organizerName" to event.organizerName,
            "description" to event.description,
            "category" to event.category.name,
            "status" to event.status.name,
            "date" to Timestamp(event.date / 1000, ((event.date % 1000) * 1_000_000).toInt()),
            "startTime" to Timestamp(event.startTime / 1000, ((event.startTime % 1000) * 1_000_000).toInt()),
            "endTime" to Timestamp(event.endTime / 1000, ((event.endTime % 1000) * 1_000_000).toInt()),
            "location" to event.location,
            "rockZoneSeats" to event.rockZoneSeats,
            "normalZoneSeats" to event.normalZoneSeats,
            "totalSeats" to event.totalSeats,
            "availableSeats" to event.availableSeats,
            "rockZonePrice" to event.rockZonePrice,
            "normalZonePrice" to event.normalZonePrice,
            "imagePath" to event.imagePath,
            "createdAt" to Timestamp(event.createdAt / 1000, ((event.createdAt % 1000) * 1_000_000).toInt()),
            "updatedAt" to Timestamp(event.updatedAt / 1000, ((event.updatedAt % 1000) * 1_000_000).toInt())
        )
    }

    private fun DocumentSnapshot.toEvent(): Event? {
        return try {
            val data = this.data ?: return null

            fun timestampToLong(field: String): Long {
                return (data[field] as? Timestamp)?.toDate()?.time ?: 0L
            }

            Event(
                eventId = this.id,
                name = data["eventName"]?.toString() ?: "",
                organizerId = data["organizerId"]?.toString() ?: "",
                organizerName = data["organizerName"]?.toString() ?: "",
                description = data["description"]?.toString() ?: "",
                category = EventCategory.valueOf(data["category"]?.toString() ?: ""),
                status = EventStatus.valueOf(data["status"]?.toString() ?: "PENDING"),
                date = timestampToLong("date"),
                startTime = timestampToLong("startTime"),
                endTime = timestampToLong("endTime"),
                location = data["location"]?.toString() ?: "",
                rockZoneSeats = (data["rockZoneSeats"] as? Number)?.toInt() ?: 0,
                normalZoneSeats = (data["normalZoneSeats"] as? Number)?.toInt() ?: 0,
                totalSeats = (data["totalSeats"] as? Number)?.toInt() ?: 0,
                availableSeats = (data["availableSeats"] as? Number)?.toInt() ?: 0,
                rockZonePrice = (data["rockZonePrice"] as? Number)?.toDouble() ?: 0.0,
                normalZonePrice = (data["normalZonePrice"] as? Number)?.toDouble() ?: 0.0,
                imagePath = data["imagePath"]?.toString()?.takeIf { it.isNotBlank() },
                createdAt = timestampToLong("createdAt"),
                updatedAt = timestampToLong("updatedAt")
            )
        } catch (e: Exception) {
            Log.e("FirebaseEventDataSource", "Error converting document ${this.id} to Event", e)
            null
        }
    }
}