package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.model.uimodel.PaymentStatistics
import kotlin.collections.filter

fun buildPaymentStatistics(
    payments: List<Payment>
): PaymentStatistics {

    val total = payments.size

    val success = payments.count {
        it.status == PaymentStatus.SUCCESS
    }

    val pending = payments.count {
        it.status == PaymentStatus.PENDING
    }

    val failed = payments.count {
        it.status == PaymentStatus.FAILED
    }

    val totalRevenue = payments
        .filter { it.status == PaymentStatus.SUCCESS }
        .sumOf { it.amount }

    val successRate =
        if (total == 0) 0.0 else success.toDouble() / total

    val pendingRate =
        if (total == 0) 0.0 else pending.toDouble() / total

    val failedRate =
        if (total == 0) 0.0 else failed.toDouble() / total

    return PaymentStatistics(
        totalPayments = total,
        successCount = success,
        pendingCount = pending,
        failedCount = failed,
        totalRevenue = totalRevenue,
        successRate = successRate,
        pendingRate = pendingRate,
        failedRate = failedRate
    )
}