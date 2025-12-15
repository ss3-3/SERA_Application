package com.example.sera_application.domain.util

import java.util.UUID
import javax.inject.Inject

class ReservationIdGenerator @Inject constructor() {
    
    fun generate(): String {
        return "RES-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(8).uppercase()}"
    }
    
    fun generateBatch(count: Int): List<String> {
        return List(count) { generate() }
    }
}
