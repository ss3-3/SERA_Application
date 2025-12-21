package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.uimodel.PaymentStatistics
import com.example.sera_application.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPaymentStatisticsUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(): Flow<PaymentStatistics> = flow {
        try {
            val stats = paymentRepository.getPaymentStatistics()
            emit(stats)
        } catch (e: Exception) {
            emit(PaymentStatistics(
                totalPayments = 0,
                successCount = 0,
                pendingCount = 0,
                failedCount = 0,
                totalRevenue = 0.0,
                successRate = 0.0,
                pendingRate = 0.0,
                failedRate = 0.0
            ))
        }
    }
}