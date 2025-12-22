package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.ReservationStatistics
import com.example.sera_application.domain.repository.ReportRepository
import javax.inject.Inject

class GetEventStatisticsUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(eventId: String): ReservationStatistics {
        return reportRepository.getEventStatistics(eventId)
    }
}