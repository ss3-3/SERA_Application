package com.example.sera_application.presentation.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.uimodel.PaymentStatistics
import com.example.sera_application.domain.model.uimodel.TopEarningEventUiModel
import com.example.sera_application.domain.usecase.report.GetPaymentStatisticsUseCase
import com.example.sera_application.domain.usecase.report.GetRevenueTrendUseCase
import com.example.sera_application.domain.usecase.report.GetTopEarningEventsUseCase
import com.example.sera_application.domain.usecase.report.GetTotalRevenueUseCase
import com.patrykandpatryk.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RevenueReportViewModel(
    private val getTotalRevenueUseCase: GetTotalRevenueUseCase,
    private val getRevenueTrendUseCase: GetRevenueTrendUseCase,
    private val getTopEarningEventsUseCase: GetTopEarningEventsUseCase,
    private val getPaymentStatisticsUseCase: GetPaymentStatisticsUseCase
) : ViewModel() {

    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()

    private val _revenueGrowth = MutableStateFlow(0.0)
    val revenueGrowth: StateFlow<Double> = _revenueGrowth.asStateFlow()

    private val _revenueData = MutableStateFlow<List<FloatEntry>>(emptyList())
    val revenueData: StateFlow<List<FloatEntry>> = _revenueData.asStateFlow()

    private val _topEarningEvents = MutableStateFlow<List<TopEarningEventUiModel>>(emptyList())
    val topEarningEvents: StateFlow<List<TopEarningEventUiModel>> = _topEarningEvents.asStateFlow()

    private val _paymentStats = MutableStateFlow<PaymentStatistics?>(null)
    val paymentStats: StateFlow<PaymentStatistics?> = _paymentStats.asStateFlow()

    init {
        loadRevenueData()
    }

    private fun loadRevenueData() {
        viewModelScope.launch {
            getTotalRevenueUseCase().collect { data ->
                _totalRevenue.value = data.total
                _revenueGrowth.value = data.growth
            }
        }

        viewModelScope.launch {
            getRevenueTrendUseCase(7).collect { trend ->
                _revenueData.value = trend
            }
        }

        viewModelScope.launch {
            getTopEarningEventsUseCase().collect { events ->
                _topEarningEvents.value = events
            }
        }

        viewModelScope.launch {
            getPaymentStatisticsUseCase().collect { stats ->
                _paymentStats.value = stats
            }
        }
    }

    fun loadTrendData(period: String) {
        viewModelScope.launch {
            val days = if (period == "Weekly") 7 else 30
            getRevenueTrendUseCase(days).collect { trend ->
                _revenueData.value = trend
            }
        }
    }
}