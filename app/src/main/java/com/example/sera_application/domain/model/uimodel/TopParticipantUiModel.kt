package com.example.sera_application.domain.model.uimodel

data class TopParticipantUiModel(
    val userId: String,
    val name: String,
    val profileImagePath: String?,
    val participationCount: Int,
    val rank: Int
)
