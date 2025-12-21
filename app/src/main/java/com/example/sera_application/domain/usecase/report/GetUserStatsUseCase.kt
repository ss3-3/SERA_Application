package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.UserRepository
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

            // Calculate new users in the last 30 days
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
            val newUsers = userRepository.getUsersCreatedBetween(thirtyDaysAgo, now)

            // Active users = unique participants
            val activeUsers = reservationRepository.getUniqueParticipantsCount()

            val totalReservations = reservationRepository.getTotalReservationCount()

            emit(UserStats(
                totalUsers = totalUsers,
                newUsers = newUsers,
                participants = activeUsers,  // 改为 participants
                totalReservations = totalReservations
            ))
        } catch (e: Exception) {
            emit(UserStats(0, 0, 0, 0))
        }
    }
}