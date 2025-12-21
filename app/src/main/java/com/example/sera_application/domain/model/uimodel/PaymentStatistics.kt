package com.example.sera_application.domain.model.uimodel

data class PaymentStatistics(
    val totalPayments: Int,
    val successCount: Int,
    val pendingCount: Int,
    val failedCount: Int,
    val totalRevenue: Double,
    val successRate: Double,
    val pendingRate: Double,
    val failedRate: Double
)
