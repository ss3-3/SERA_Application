package com.example.sera_application.utils

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.domain.model.enums.EventStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseEventInitializer {
    suspend fun initializePredefinedEvents(firestore: FirebaseFirestore) {
        val eventsRef = firestore.collection("events")
        
        // Check if events already exist
        val snapshot = eventsRef.limit(1).get().await()
        if (!snapshot.isEmpty) return

        val predefinedEvents = listOf(
            Event(
                name = "SERA Opening Ceremony",
                organizerId = "admin",
                organizerName = "SERA Team",
                description = "Grand opening of the Student Event Reservation App.",
                category = EventCategory.FESTIVAL,
                status = EventStatus.APPROVED,
                date = "25/12/2025",
                startTime = "10:00 AM",
                endTime = "12:00 PM",
                duration = "2 hours",
                location = "Main Hall, TARUMT",
                rockZoneSeats = 100,
                normalZoneSeats = 300,
                totalSeats = 400,
                availableSeats = 400,
                rockZonePrice = 0.0,
                normalZonePrice = 0.0
            ),
            Event(
                name = "Career Fair 2025",
                organizerId = "admin",
                organizerName = "Career Center",
                description = "Meet top employers and explore career opportunities.",
                category = EventCategory.CAREER,
                status = EventStatus.APPROVED,
                date = "15/01/2026",
                startTime = "09:00 AM",
                endTime = "05:00 PM",
                duration = "8 hours",
                location = "Exhibition Hall, TARUMT",
                rockZoneSeats = 0,
                normalZoneSeats = 1000,
                totalSeats = 1000,
                availableSeats = 1000,
                rockZonePrice = 0.0,
                normalZonePrice = 0.0
            )
        )

        for (event in predefinedEvents) {
            val docRef = eventsRef.document()
            val eventWithId = event.copy(eventId = docRef.id)
            docRef.set(eventWithId).await()
        }
    }
}
