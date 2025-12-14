package com.example.sera_application.domain.model.enums

enum class EventStatus {
    PENDING,
    APPROVED,
    REJECTED,
//    ACTIVE,
//    FULL,
//    COMPLETED,
//    CANCELLED,
//    ONGOING
}

// Add event category
enum class EventCategory(val displayName: String) {
    ACADEMIC("Academic"),
    CAREER("Career"),
    ART("Art"),
    WELLNESS("Wellness"),
    MUSIC("Music"),
    FESTIVAL("Festival");

    companion object {
        fun fromDisplayName(name: String): EventCategory? {
            return values().find { it.displayName.equals(name, ignoreCase = true) }
        }
    }
}