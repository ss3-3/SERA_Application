package com.example.sera_application.domain.model.uimodel

data class TopEarningEventUiModel(
    val eventId: String,
    val name: String,
    val rank: Int,
    val imagePath: String?,
    val revenue: Double
)
