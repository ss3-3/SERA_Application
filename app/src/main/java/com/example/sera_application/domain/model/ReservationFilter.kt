package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.ReservationStatus

data class ReservationFilter(
    val userId: String? = null,
    val eventId: String? = null,
    val status: ReservationStatus? = null,
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null
)
