package com.example.sera_application.domain.usecase.report

import androidx.compose.ui.graphics.Color
import com.example.sera_application.domain.model.uimodel.Item
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAdminStatsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(): Flow<List<Item>> = flow {
        try {
            val totalUsers = userRepository.getTotalUserCount()
            val totalReservations = reservationRepository.getTotalReservationCount()
            val totalRevenue = paymentRepository.getTotalRevenue()
            val totalEvents = eventRepository.getTotalEventCount()

            emit(listOf(
                Item("Total Users", totalUsers.toString(),
                    Color(0xFFEC8282), Color(0xFFDB2020)),
                Item("Total Reservations", totalReservations.toString(),
                    Color(0xFFB8E7B7), Color(0xFF247411)),
                Item("Total Revenue", totalRevenue.toInt().toString(),
                    Color(0xFFB5CAD7), Color(0xFF2777A8)),
                Item("Total Events", totalEvents.toString(),
                    Color(0xFFDED8BC), Color(0xFFA59217))
            ))
        } catch (e: Exception) {
            // Emit empty list on error to prevent crash
            emit(emptyList())
        }
    }
}