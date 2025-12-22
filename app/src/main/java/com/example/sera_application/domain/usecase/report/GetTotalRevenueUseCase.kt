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
    suspend fun getTotalRevenueForEvent(eventId: String? = null): Double {
        return reportRepository.getTotalRevenue(eventId)
    }

    operator fun invoke(): Flow<RevenueData> = flow {
        try {
            val allPayments = try {
                paymentRepository.getAllPayments()
            } catch (e: Exception) {
                android.util.Log.e("GetTotalRevenueUseCase", "Error getting all payments: ${e.message}", e)
                emptyList()
            }
            
            val paymentStats = try {
                paymentRepository.getPaymentStatistics()
            } catch (e: Exception) {
                android.util.Log.e("GetTotalRevenueUseCase", "Error getting payment statistics: ${e.message}", e)
                com.example.sera_application.domain.model.uimodel.PaymentStatistics(
                    totalPayments = 0,
                    successCount = 0,
                    pendingCount = 0,
                    failedCount = 0,
                    totalRevenue = 0.0,
                    successRate = 0.0,
                    pendingRate = 0.0,
                    failedRate = 0.0
                )
            }
            
            val currentTotal = paymentStats.totalRevenue

            // Calculate growth (simplified: compare with previous period)
            val previousPeriodTotal = try {
                allPayments
                    .filter { it.createdAt < System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 }
                    .sumOf { it.amount }
            } catch (e: Exception) {
                android.util.Log.e("GetTotalRevenueUseCase", "Error calculating previous period total: ${e.message}", e)
                0.0
            }

            val growth = if (previousPeriodTotal > 0) {
                try {
                    ((currentTotal - previousPeriodTotal) / previousPeriodTotal) * 100
                } catch (e: Exception) {
                    android.util.Log.e("GetTotalRevenueUseCase", "Error calculating growth: ${e.message}", e)
                    0.0
                }
            } else {
                0.0
            }

            emit(RevenueData(
                total = currentTotal,
                growth = growth
            ))
        } catch (e: Exception) {
            android.util.Log.e("GetTotalRevenueUseCase", "Unexpected error in GetTotalRevenueUseCase: ${e.message}", e)
            emit(RevenueData(0.0, 0.0))
        }
    }
}