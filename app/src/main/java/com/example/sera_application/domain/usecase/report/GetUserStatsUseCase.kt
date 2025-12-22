package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.UserRepository
import com.example.sera_application.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class UserStats(
    val totalUsers: Int,
    val newUsers: Int,
    val participants: Int,
    val totalReservations: Int
)

class GetUserStatsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val reservationRepository: ReservationRepository
) {
    operator fun invoke(month: String? = null): Flow<UserStats> = flow {
        try {
            val totalUsers = userRepository.getTotalUserCount()

            // Calculate new users based on month parameter
            val newUsers = if (month != null) {
                // Use month-specific date range
                val (startTimestamp, endTimestamp) = DateUtils.parseMonthToTimestamp(month) 
                    ?: run {
                        // Fallback to last 30 days if parsing fails
                        val now = System.currentTimeMillis()
                        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
                        Pair(thirtyDaysAgo, now)
                    }
                userRepository.getUsersCreatedBetween(startTimestamp, endTimestamp)
            } else {
                // Default: last 30 days
                val now = System.currentTimeMillis()
                val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
                userRepository.getUsersCreatedBetween(thirtyDaysAgo, now)
            }

            // Active users = unique participants
            val activeUsers = reservationRepository.getUniqueParticipantsCount()

            val totalReservations = reservationRepository.getTotalReservationCount()

            emit(UserStats(
                totalUsers = totalUsers,
                newUsers = newUsers,
                participants = activeUsers,
                totalReservations = totalReservations
            ))
        } catch (e: Exception) {
            emit(UserStats(0, 0, 0, 0))
        }
    }
}