package com.example.sera_application.domain.model.uimodel

data class EventListUiModel(
    val title: String,
    val picture: String,
    val organizer: String,
    val description: String,
    val revenue: Double,
    val participants: Int
)
