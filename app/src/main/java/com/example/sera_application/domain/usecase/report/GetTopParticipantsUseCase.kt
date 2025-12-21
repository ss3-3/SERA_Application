package com.example.sera_application.domain.usecase.report

import com.example.sera_application.data.local.UserParticipation
import com.example.sera_application.domain.model.uimodel.TopParticipantUiModel
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTopParticipantsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<TopParticipantUiModel>> = flow {
        try {
            val topParticipants = reservationRepository.getTopParticipants(limit)

            val uiModels = topParticipants.mapIndexed { index, participation ->
                val user = userRepository.getUserById(participation.userId)

                TopParticipantUiModel(
                    userId = participation.userId,
                    name = user?.fullName ?: "Unknown User",
                    profileImagePath = user?.profileImagePath,
                    participationCount = participation.eventCount,
                    rank = index + 1
                )
            }

            emit(uiModels)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}