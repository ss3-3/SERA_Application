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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import android.util.Log

@HiltViewModel
class RevenueReportViewModel @Inject constructor(
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
        try {
            Log.d("RevenueReportViewModel", "RevenueReportViewModel initialized")
        } catch (e: Exception) {
            Log.e("RevenueReportViewModel", "Error during initialization: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun loadRevenueData() {
        viewModelScope.launch {
            getTotalRevenueUseCase()
                .catch { exception ->
                    Log.e("RevenueReportViewModel", "Error loading total revenue: ${exception.message}", exception)
                    _totalRevenue.value = 0.0
                    _revenueGrowth.value = 0.0
                }
                .collect { data ->
                    _totalRevenue.value = data.total
                    _revenueGrowth.value = data.growth
                }
        }

        viewModelScope.launch {
            getRevenueTrendUseCase(7)
                .catch { exception ->
                    Log.e("RevenueReportViewModel", "Error loading revenue trend: ${exception.message}", exception)
                    _revenueData.value = emptyList()
                }
                .collect { trend ->
                    _revenueData.value = trend
                }
        }

        viewModelScope.launch {
            getTopEarningEventsUseCase()
                .catch { exception ->
                    Log.e("RevenueReportViewModel", "Error loading top earning events: ${exception.message}", exception)
                    _topEarningEvents.value = emptyList()
                }
                .collect { events ->
                    _topEarningEvents.value = events
                }
        }

        viewModelScope.launch {
            getPaymentStatisticsUseCase()
                .catch { exception ->
                    Log.e("RevenueReportViewModel", "Error loading payment statistics: ${exception.message}", exception)
                    _paymentStats.value = null
                }
                .collect { stats ->
                    _paymentStats.value = stats
                }
        }
    }

    fun loadTrendData(period: String) {
        viewModelScope.launch {
            val days = if (period == "Weekly") 7 else 30
            getRevenueTrendUseCase(days)
                .catch { exception ->
                    Log.e("RevenueReportViewModel", "Error loading trend data: ${exception.message}", exception)
                    _revenueData.value = emptyList()
                }
                .collect { trend ->
                    _revenueData.value = trend
                }
        }
    }
}