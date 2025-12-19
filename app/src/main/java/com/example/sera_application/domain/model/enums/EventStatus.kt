package com.example.sera_application.domain.model.enums

import androidx.compose.ui.graphics.Color

enum class EventStatus(
    val label: String,
    val color: Color
) {
    PENDING(
        label = "Pending",
        color = Color(0xFFFFA726) // Orange
    ),
    APPROVED(
        label = "Approved",
        color = Color(0xFF66BB6A) // Green
    ),
    REJECTED(
        label = "Rejected",
        color = Color(0xFFEF5350) // Red
    ),
    COMPLETED(
        label = "Completed",
        color = Color(0xFF42A5F5) // Blue
    ),
    CANCELLED(
        label = "Cancelled",
        color = Color(0xFF9E9E9E) // Grey
    )
}

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
