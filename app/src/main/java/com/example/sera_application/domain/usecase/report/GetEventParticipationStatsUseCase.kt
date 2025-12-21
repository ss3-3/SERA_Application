package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class EventParticipationStats(
    val participatingUsers: Int,
    val userPercentage: Double,
    val averageEventsPerUser: Double,
    val totalBookings: Int
)

class GetEventParticipationStatsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<EventParticipationStats> = flow {
        try {
            val totalUsers = userRepository.getTotalUserCount()
            val participatingUsers = reservationRepository.getUniqueParticipantsCount()
            val totalBookings = reservationRepository.getTotalReservationCount()

            val userPercentage = if (totalUsers > 0) {
                (participatingUsers.toDouble() / totalUsers) * 100
            } else {
                0.0
            }

            val averageEventsPerUser = if (participatingUsers > 0) {
                totalBookings.toDouble() / participatingUsers
            } else {
                0.0
            }

            emit(EventParticipationStats(
                participatingUsers = participatingUsers,
                userPercentage = userPercentage,
                averageEventsPerUser = averageEventsPerUser,
                totalBookings = totalBookings
            ))
        } catch (e: Exception) {
            emit(EventParticipationStats(0, 0.0, 0.0, 0))
        }
    }
}