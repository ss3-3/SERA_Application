package com.example.sera_application.presentation.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.uimodel.Item
import com.example.sera_application.domain.usecase.report.GetAdminStatsUseCase
import com.example.sera_application.domain.usecase.report.GetPopularEventsUseCase
import com.example.sera_application.domain.usecase.report.GetTrendDataUseCase
import com.patrykandpatryk.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val getAdminStatsUseCase: GetAdminStatsUseCase,
    private val getTrendDataUseCase: GetTrendDataUseCase,
    private val getPopularEventsUseCase: GetPopularEventsUseCase
) : ViewModel() {

    private val _statsItems = MutableStateFlow<List<Item>>(emptyList())
    val statsItems: StateFlow<List<Item>> = _statsItems.asStateFlow()

    private val _bookingData = MutableStateFlow<List<FloatEntry>>(emptyList())
    val bookingData: StateFlow<List<FloatEntry>> = _bookingData.asStateFlow()

    private val _userGrowthData = MutableStateFlow<List<FloatEntry>>(emptyList())
    val userGrowthData: StateFlow<List<FloatEntry>> = _userGrowthData.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            getAdminStatsUseCase().collect { stats ->
                _statsItems.value = stats
            }
        }

        viewModelScope.launch {
            getTrendDataUseCase().collect { trends ->
                _bookingData.value = trends.bookings
                _userGrowthData.value = trends.users
            }
        }
    }
}