package com.example.sera_application.data.remote.firebase

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
        // Convert to Firestore-compatible map
        val eventMap = eventToFirestoreMap(eventWithId)
        docRef.set(eventMap).await()
        return docRef.id
    }

    override suspend fun updateEvent(event: Event) {
        // Convert to Firestore-compatible map
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

    /**
     * Convert Event domain model to Firestore-compatible map
     * Handles field name mapping (name -> eventName) for Firebase
     */
    private fun eventToFirestoreMap(event: Event): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()

        map["eventId"] = event.eventId
        map["eventName"] = event.name  // Firebase uses eventName, not name
        map["organizerId"] = event.organizerId
        map["organizerName"] = event.organizerName
        map["description"] = event.description
        map["category"] = event.category.name  // Store enum as string
        map["status"] = event.status.name  // Store enum as string
        
        // Date & Time
        map["date"] = event.date
        map["startTime"] = event.startTime
        map["endTime"] = event.endTime
        map["duration"] = event.duration
        
        // Location
        map["location"] = event.location
        
        // Seats
        map["rockZoneSeats"] = event.rockZoneSeats
        map["normalZoneSeats"] = event.normalZoneSeats
        map["totalSeats"] = event.totalSeats
        map["availableSeats"] = event.availableSeats
        
        // Pricing
        map["rockZonePrice"] = event.rockZonePrice
        map["normalZonePrice"] = event.normalZonePrice
        
        // Media
        map["imagePath"] = event.imagePath ?: ""
        
        // Timestamps (convert to Timestamp for Firestore)
        fun longToTimestamp(millis: Long): Timestamp {
            return Timestamp(
                millis / 1000,
                ((millis % 1000) * 1_000_000).toInt()
            )
        }
        
        map["createdAt"] = longToTimestamp(event.createdAt)
        map["updatedAt"] = longToTimestamp(event.updatedAt)

        return map
    }

    /**
     * Convert Firestore document to Event domain model
     * Handles field name mapping (eventName -> name) and enum conversions
     */
    private fun DocumentSnapshot.toEvent(): Event? {
        return try {
            val data = this.data ?: return null

            // Convert Timestamp to Long (milliseconds)
            fun timestampToLong(timestamp: Any?): Long {
                return when (timestamp) {
                    is Timestamp -> timestamp.seconds * 1000 + timestamp.nanoseconds / 1_000_000
                    is Long -> timestamp
                    else -> System.currentTimeMillis()
                }
            }

            // Parse category enum
            val categoryStr = data["category"]?.toString() ?: return null
            val category = try {
                EventCategory.valueOf(categoryStr)
            } catch (e: Exception) {
                return null
            }

            // Parse status enum
            val statusStr = data["status"]?.toString() ?: "PENDING"
            val status = try {
                EventStatus.valueOf(statusStr)
            } catch (e: Exception) {
                EventStatus.PENDING
            }

            Event(
                eventId = this.id,
                name = data["eventName"]?.toString() ?: return null, // Firebase uses "eventName"
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
            null
        }
    }
}
