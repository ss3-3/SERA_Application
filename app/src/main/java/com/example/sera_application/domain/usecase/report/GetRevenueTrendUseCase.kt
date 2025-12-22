package com.example.sera_application.domain.usecase.report

import android.util.Log
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
            Log.w("GetRevenueTrendUseCase", "Invalid days parameter: $days")
            emit(emptyList())
            return@flow
        }

        try {
            val startDate = System.currentTimeMillis() - days.toLong() * 24 * 60 * 60 * 1000
            Log.d("GetRevenueTrendUseCase", "Getting revenue trend for $days days, startDate: $startDate")
            
            val revenueTrend = paymentRepository.getRevenueTrend(days, startDate)
            
            Log.d("GetRevenueTrendUseCase", "Received ${revenueTrend.size} revenue data points")

            val floatEntries = revenueTrend.mapIndexed { index, revenue ->
                FloatEntry(index.toFloat(), revenue.toFloat())
            }

            Log.d("GetRevenueTrendUseCase", "Created ${floatEntries.size} FloatEntry objects")
            emit(floatEntries)
        } catch (e: Exception) {
            Log.e("GetRevenueTrendUseCase", "Error getting revenue trend: ${e.message}", e)
            emit(emptyList())
        }
    }
}