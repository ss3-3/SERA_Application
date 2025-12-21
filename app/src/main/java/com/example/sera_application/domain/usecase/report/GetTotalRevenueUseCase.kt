package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.repository.ReportRepository
import com.example.sera_application.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class RevenueData(
    val total: Double,
    val growth: Double
)

class GetTotalRevenueUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(eventId: String? = null): Double {
        return reportRepository.getTotalRevenue(eventId)
    }

    operator fun invoke(): Flow<RevenueData> = flow {
        try {
            val allPayments = paymentRepository.getAllPayments()
            val currentTotal = paymentRepository.getPaymentStatistics().totalRevenue

            // Calculate growth (simplified: compare with previous period)
            val previousPeriodTotal = allPayments
                .filter { it.createdAt < System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 }
                .sumOf { it.amount }

            val growth = if (previousPeriodTotal > 0) {
                ((currentTotal - previousPeriodTotal) / previousPeriodTotal) * 100
            } else {
                0.0
            }

            emit(RevenueData(
                total = currentTotal,
                growth = growth
            ))
        } catch (e: Exception) {
            emit(RevenueData(0.0, 0.0))
        }
    }
}