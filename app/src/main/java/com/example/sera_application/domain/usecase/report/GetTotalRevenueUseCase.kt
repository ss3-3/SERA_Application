package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.repository.ReportRepository
import javax.inject.Inject

class GetTotalRevenueUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(eventId: String? = null): Double {
        return reportRepository.getTotalRevenue(eventId)
    }
}

