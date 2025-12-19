package com.example.sera_application.data.util

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.domain.model.enums.EventStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utility class to initialize predefined events in Firebase Firestore.
 * Call initializePredefinedEvents() to create 4 sample events.
 */
object FirebaseEventInitializer {

    /**
     * Initialize 4 predefined events in Firebase Firestore.
     * This should be called once (e.g., during app initialization or development).
     */
    suspend fun initializePredefinedEvents(firestore: FirebaseFirestore) {
        val eventsRef = firestore.collection("events")

        val predefinedEvents = createPredefinedEvents()

        predefinedEvents.forEach { event ->
            try {
                // Check if event already exists
                val existingDoc = eventsRef.document(event.eventId).get().await()
                if (!existingDoc.exists()) {
                    // Convert Event to Firestore-compatible map
                    val eventMap = eventToFirestoreMap(event)
                    eventsRef.document(event.eventId).set(eventMap).await()
                    println("✅ Created event: ${event.name} (${event.eventId})")
                } else {
                    println("⚠️ Event already exists: ${event.name} (${event.eventId})")
                }
            } catch (e: Exception) {
                println("❌ Error creating event ${event.name}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Create 4 predefined events based on your Firebase structure
     */
    private fun createPredefinedEvents(): List<Event> {
        val currentTime = System.currentTimeMillis()

        return listOf(
            // Event 1: Music Fiesta 6.0
            Event(
                eventId = "event_001",
                name = "Music Fiesta 6.0",
                organizerId = "organizer_1",
                organizerName = "Music Society TARUMT",
                description = "Music Fiesta 6.0 is a large-scale campus concert and carnival proudly organized by Music Society of Tunku Abdul Rahman University of Management and Technology (TARUMT).",
                category = EventCategory.MUSIC,
                status = EventStatus.APPROVED,
                date = "28/12/2025",
                startTime = "19:00 PM",
                endTime = "22:00 PM",
                duration = "3 hour(s)",
                location = "ARENA",
                rockZoneSeats = 0,
                normalZoneSeats = 500,
                totalSeats = 500,
                availableSeats = 500,
                rockZonePrice = 0.0,
                normalZonePrice = 20.0,
                imagePath = "musicfiesta",
                createdAt = currentTime,
                updatedAt = currentTime
            ),

            // Event 2: Academic Workshop
            Event(
                eventId = "event_002",
                name = "GOTAR Festival",
                organizerId = "organizer_2",
                organizerName = "Business Administration Society",
                description = "GOTAR Festival is a vibrant celebration featuring exciting dance and singing performances, live entertainment, and a variety of food and beverage stalls. Join us for a fun-filled day of music, culture, and great vibes!",
                category = EventCategory.FESTIVAL,
                status = EventStatus.APPROVED,
                date = "15/01/2026",
                startTime = "09:00 AM",
                endTime = "12:00 PM",
                duration = "3 hour(s)",
                location = "DSA",
                rockZoneSeats = 0,
                normalZoneSeats = 200,
                totalSeats = 200,
                availableSeats = 200,
                rockZonePrice = 0.0,
                normalZonePrice = 15.0,
                imagePath = "gotar",
                createdAt = currentTime,
                updatedAt = currentTime
            ),

            // Event 3: Career Fair
            Event(
                eventId = "event_003",
                name = "Internship Fair",
                organizerId = "organizer_3",
                organizerName = "Student Career Development Centre",
                description = "Meet top employers, discover job opportunities, and network with industry professionals at our annual career fair.",
                category = EventCategory.CAREER,
                status = EventStatus.APPROVED,
                date = "20/02/2026",
                startTime = "10:00 AM",
                endTime = "04:00 PM",
                duration = "6 hour(s)",
                location = "SPORT_COMPLEX",
                rockZoneSeats = 0,
                normalZoneSeats = 300,
                totalSeats = 300,
                availableSeats = 300,
                rockZonePrice = 0.0,
                normalZonePrice = 10.0,
                imagePath = "sodc",
                createdAt = currentTime,
                updatedAt = currentTime
            ),

            // Event 4: Wellness Festival
            Event(
                eventId = "event_004",
                name = "Voichestra Festival",
                organizerId = "organizer_4",
                organizerName = "TARUC Choir Society",
                description = "Enjoy a day of beautiful harmonies at Voichestra Festival, featuring choir performances, live singing, and an exciting festival atmosphere for music lovers.",
                category = EventCategory.MUSIC,
                status = EventStatus.APPROVED,
                date = "05/03/2026",
                startTime = "08:00 AM",
                endTime = "05:00 PM",
                duration = "9 hour(s)",
                location = "RIMBA",
                rockZoneSeats = 100,
                normalZoneSeats = 300,
                totalSeats = 400,
                availableSeats = 400,
                rockZonePrice = 25.0,
                normalZonePrice = 15.0,
                imagePath = "voichestra",
                createdAt = currentTime,
                updatedAt = currentTime
            )
        )
    }

    /**
     * Convert Event domain model to Firestore-compatible map
     * This ensures proper field mapping to match Firebase structure
     */
    private fun eventToFirestoreMap(event: Event): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()

        // Map fields to match Firebase structure
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
        map["createdAt"] = Timestamp(
            event.createdAt / 1000, 
            ((event.createdAt % 1000) * 1_000_000).toInt()
        )
        map["updatedAt"] = Timestamp(
            event.updatedAt / 1000, 
            ((event.updatedAt % 1000) * 1_000_000).toInt()
        )

        return map
    }

    /**
     * Helper function to be called from Application class or MainActivity
     * Example usage:
     * ```
     * // In SeraApplication.onCreate() or MainActivity.onCreate()
     * lifecycleScope.launch {
     *     FirebaseEventInitializer.initializePredefinedEvents(
     *         FirebaseFirestore.getInstance()
     *     )
     * }
     * ```
     */
    suspend fun initializeOnce(firestore: FirebaseFirestore) {
        initializePredefinedEvents(firestore)
    }
}



