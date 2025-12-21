package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.repository.PaymentRepository
import com.patrykandpatryk.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetRevenueTrendUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(days: Int): Flow<List<FloatEntry>> = flow {
        if (days <= 0) {
            emit(emptyList())
            return@flow
        }

        try {
            val startDate = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
            val revenueTrend = paymentRepository.getRevenueTrend(days, startDate)

            val floatEntries = revenueTrend.mapIndexed { index, revenue ->
                FloatEntry(index.toFloat(), revenue.toFloat())
            }

            emit(floatEntries)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}