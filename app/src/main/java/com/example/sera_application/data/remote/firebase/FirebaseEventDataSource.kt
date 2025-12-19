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
        eventsRef.document(event.eventId)
            .set(eventMap)
            .await()
    }

    override suspend fun deleteEvent(eventId: String) {
        eventsRef.document(eventId).delete().await()
    }

    override suspend fun getEventList(): List<Event> {
        val snapshot = eventsRef.get().await()
        return snapshot.documents.mapNotNull { documentSnapshot ->
            documentSnapshot.toEvent()
        }
    }

    override suspend fun getEventById(eventId: String): Event? {
        val document = eventsRef.document(eventId).get().await()
        return if (document.exists()) {
            document.toEvent()
        } else {
            null
        }
    }

    override suspend fun getEventsByOrganizer(organizerId: String): List<Event> {
        val snapshot = eventsRef
            .whereEqualTo("organizerId", organizerId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { documentSnapshot ->
            documentSnapshot.toEvent()
        }
    }

    override suspend fun updateEventStatus(eventId: String, status: String) {
        eventsRef.document(eventId)
            .update("status", status)
            .await()
    }

    override suspend fun getEventListFromFirebase(): List<Event> {
        val snapshot = eventsRef.get().await()
        return snapshot.documents.mapNotNull { it.toEvent() }
    }

    override suspend fun getPublicEvents(): List<Event> {
        return firestore.collection("events")
            .whereEqualTo("status", EventStatus.APPROVED.name)
            .limit(10)
            .get()
            .await()
            .toObjects(Event::class.java)
    }

    private fun eventToFirestoreMap(event: Event): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["eventId"] = event.eventId
        map["eventName"] = event.name
        map["organizerId"] = event.organizerId
        map["organizerName"] = event.organizerName
        map["description"] = event.description
        map["category"] = event.category.name
        map["status"] = event.status.name
        map["date"] = event.date
        map["startTime"] = event.startTime
        map["endTime"] = event.endTime
        map["duration"] = event.duration
        map["location"] = event.location
        map["rockZoneSeats"] = event.rockZoneSeats
        map["normalZoneSeats"] = event.normalZoneSeats
        map["totalSeats"] = event.totalSeats
        map["availableSeats"] = event.availableSeats
        map["rockZonePrice"] = event.rockZonePrice
        map["normalZonePrice"] = event.normalZonePrice
        map["imagePath"] = event.imagePath ?: ""
        map["createdAt"] = Timestamp.now()
        map["updatedAt"] = Timestamp.now()
        return map
    }

    private fun DocumentSnapshot.toEvent(): Event? {
        return try {
            val data = this.data ?: return null

            fun timestampToLong(timestamp: Any?): Long {
                return when (timestamp) {
                    is Timestamp -> timestamp.seconds * 1000 + timestamp.nanoseconds / 1_000_000
                    is Long -> timestamp
                    else -> System.currentTimeMillis()
                }
            }

            val categoryStr = data["category"]?.toString() ?: return null
            val category = EventCategory.valueOf(categoryStr)

            val statusStr = data["status"]?.toString() ?: "PENDING"
            val status = EventStatus.valueOf(statusStr)

            Event(
                eventId = this.id,
                name = data["eventName"]?.toString() ?: return null,
                organizerId = data["organizerId"]?.toString() ?: "",
                organizerName = data["organizerName"]?.toString() ?: "",
                description = data["description"]?.toString() ?: "",
                category = category,
                status = status,
                date = data["date"]?.toString() ?: "",
                startTime = data["startTime"]?.toString() ?: "",
                endTime = data["endTime"]?.toString() ?: "",
                duration = data["duration"]?.toString() ?: "",
                location = data["location"]?.toString() ?: "",
                rockZoneSeats = (data["rockZoneSeats"] as? Number)?.toInt() ?: 0,
                normalZoneSeats = (data["normalZoneSeats"] as? Number)?.toInt() ?: 0,
                totalSeats = (data["totalSeats"] as? Number)?.toInt() ?: 0,
                availableSeats = (data["availableSeats"] as? Number)?.toInt() ?: 0,
                rockZonePrice = (data["rockZonePrice"] as? Number)?.toDouble() ?: 0.0,
                normalZonePrice = (data["normalZonePrice"] as? Number)?.toDouble() ?: 0.0,
                imagePath = data["imagePath"]?.toString(),
                createdAt = timestampToLong(data["createdAt"]),
                updatedAt = timestampToLong(data["updatedAt"])
            )
        } catch (e: Exception) {
            Log.e("FirebaseEventDataSource", "Error converting document ${this.id}", e)
            null
        }
    }
}