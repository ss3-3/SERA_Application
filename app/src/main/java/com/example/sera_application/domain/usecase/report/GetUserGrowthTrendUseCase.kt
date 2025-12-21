package com.example.sera_application.domain.usecase.report

import com.example.sera_application.data.local.UserGrowthData
import com.example.sera_application.domain.repository.UserRepository
import com.patrykandpatryk.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class UserGrowthTrend(
    val totalUsers: List<FloatEntry>,
    val newUsers: List<FloatEntry>
)

class GetUserGrowthTrendUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(days: Int): Flow<UserGrowthTrend> = flow {
        try {
            val startDate = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
            val growthData = userRepository.getUserGrowthTrend(days, startDate)

            val totalUsersData = growthData.mapIndexed { index, data ->
                FloatEntry(index.toFloat(), data.totalUsers.toFloat())
            }

            val newUsersData = growthData.mapIndexed { index, data ->
                FloatEntry(index.toFloat(), data.newUsers.toFloat())
            }

            emit(UserGrowthTrend(totalUsersData, newUsersData))
        } catch (e: Exception) {
            emit(UserGrowthTrend(emptyList(), emptyList()))
        }
    }
}