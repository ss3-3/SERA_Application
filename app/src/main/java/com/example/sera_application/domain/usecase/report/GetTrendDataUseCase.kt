package com.example.sera_application.domain.usecase.report

import com.example.sera_application.data.local.dao.ReservationDao
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.UserRepository
import com.patrykandpatryk.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class TrendData(
    val bookings: List<FloatEntry>,
    val users: List<FloatEntry>
)

class GetTrendDataUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<TrendData> = flow {
        try {
            val startDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            val reservationTrend = reservationRepository.getMonthlyReservationTrend(startDate)
            val userGrowthTrend = userRepository.getMonthlyUserGrowthTrend(startDate)

            val bookingData = reservationTrend.mapIndexed { index, count ->
                FloatEntry(index.toFloat(), count.toFloat())
            }

            val userGrowthData = userGrowthTrend.mapIndexed { index, count ->
                FloatEntry(index.toFloat(), count.toFloat())
            }

            emit(TrendData(bookingData, userGrowthData))
        } catch (e: Exception) {
            emit(TrendData(emptyList(), emptyList()))
        }
    }
}