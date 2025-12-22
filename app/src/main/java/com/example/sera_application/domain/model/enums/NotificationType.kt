package com.example.sera_application.domain.model.enums

enum class NotificationType {
    EVENT_UPDATE,        // event approved, updated, cancelled
    RESERVATION_UPDATE,  // reservation confirmed/cancelled
    PAYMENT_UPDATE,      // payment success/refund
    ORGANIZER_APPROVAL,  // organizer account approved/rejected
    SYSTEM               // admin or system-wide message
}