package com.example.sera_application.data.local

data class UserParticipation(
    val userId: String,
    val eventCount: Int
)

data class EventRevenue(
    val eventId: String,
    val revenue: Double
)

data class UserGrowthData(
    val date: String,
    val newUsers: Int,
    val totalUsers: Int
)
