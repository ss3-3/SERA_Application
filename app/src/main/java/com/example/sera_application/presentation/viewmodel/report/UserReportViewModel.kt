package com.example.sera_application.presentation.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.uimodel.TopParticipantUiModel
import com.example.sera_application.domain.usecase.report.GetEventParticipationStatsUseCase
import com.example.sera_application.domain.usecase.report.GetTopParticipantsUseCase
import com.example.sera_application.domain.usecase.report.GetUserGrowthTrendUseCase
import com.example.sera_application.domain.usecase.report.GetUserStatsUseCase
import com.example.sera_application.domain.usecase.report.ReportConstants
import com.example.sera_application.utils.DateUtils
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
class UserReportViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getUserGrowthTrendUseCase: GetUserGrowthTrendUseCase,
    private val getTopParticipantsUseCase: GetTopParticipantsUseCase,
    private val getEventParticipationStatsUseCase: GetEventParticipationStatsUseCase
) : ViewModel() {

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers: StateFlow<Int> = _totalUsers.asStateFlow()

    private val _newUsers = MutableStateFlow(0)
    val newUsers: StateFlow<Int> = _newUsers.asStateFlow()

    private val _participants = MutableStateFlow(0)
    val participants: StateFlow<Int> = _participants.asStateFlow()

    // Add states for last month data
    private val _lastMonthNewUsers = MutableStateFlow(0)
    val lastMonthNewUsers: StateFlow<Int> = _lastMonthNewUsers.asStateFlow()

    private val _lastMonthParticipants = MutableStateFlow(0)
    val lastMonthParticipants: StateFlow<Int> = _lastMonthParticipants.asStateFlow()

    private val _totalUsersData = MutableStateFlow<List<FloatEntry>>(emptyList())
    val totalUsersData: StateFlow<List<FloatEntry>> = _totalUsersData.asStateFlow()

    private val _newUsersData = MutableStateFlow<List<FloatEntry>>(emptyList())
    val newUsersData: StateFlow<List<FloatEntry>> = _newUsersData.asStateFlow()

    private val _topParticipants = MutableStateFlow<List<TopParticipantUiModel>>(emptyList())
    val topParticipants: StateFlow<List<TopParticipantUiModel>> = _topParticipants.asStateFlow()

    private val _participatingUsers = MutableStateFlow(0)
    val participatingUsers: StateFlow<Int> = _participatingUsers.asStateFlow()

    private val _userPercentage = MutableStateFlow(0.0)
    val userPercentage: StateFlow<Double> = _userPercentage.asStateFlow()

    private val _averageEventsPerUser = MutableStateFlow(0.0)
    val averageEventsPerUser: StateFlow<Double> = _averageEventsPerUser.asStateFlow()

    private val _totalBookings = MutableStateFlow(0)
    val totalBookings: StateFlow<Int> = _totalBookings.asStateFlow()

    fun loadUserData(month: String? = null) {
        viewModelScope.launch {
            getUserStatsUseCase(month)
                .catch { exception ->
                    Log.e("UserReportViewModel", "Error loading user stats: ${exception.message}", exception)
                    _totalUsers.value = 0
                    _newUsers.value = 0
                    _participants.value = 0
                }
                .collect { stats ->
                    _totalUsers.value = stats.totalUsers
                    _newUsers.value = stats.newUsers
                    _participants.value = stats.participants
                }
        }

        // Load last month data for comparison
        viewModelScope.launch {
            getUserStatsUseCase(getPreviousMonth(month))
                .catch { exception ->
                    Log.e("UserReportViewModel", "Error loading last month stats: ${exception.message}", exception)
                    _lastMonthNewUsers.value = 0
                    _lastMonthParticipants.value = 0
                }
                .collect { lastMonthStats ->
                    _lastMonthNewUsers.value = lastMonthStats.newUsers
                    _lastMonthParticipants.value = lastMonthStats.participants
                }
        }

        viewModelScope.launch {
            getUserGrowthTrendUseCase(ReportConstants.DEFAULT_TREND_DAYS)
                .catch { exception ->
                    Log.e("UserReportViewModel", "Error loading user growth trend: ${exception.message}", exception)
                    _totalUsersData.value = emptyList()
                    _newUsersData.value = emptyList()
                }
                .collect { trends ->
                    _totalUsersData.value = trends.totalUsers
                    _newUsersData.value = trends.newUsers
                }
        }

        viewModelScope.launch {
            getTopParticipantsUseCase()
                .catch { exception ->
                    Log.e("UserReportViewModel", "Error loading top participants: ${exception.message}", exception)
                    _topParticipants.value = emptyList()
                }
                .collect { participants ->
                    _topParticipants.value = participants
                }
        }

        viewModelScope.launch {
            getEventParticipationStatsUseCase()
                .catch { exception ->
                    Log.e("UserReportViewModel", "Error loading event participation stats: ${exception.message}", exception)
                    _participatingUsers.value = 0
                    _userPercentage.value = 0.0
                    _averageEventsPerUser.value = 0.0
                    _totalBookings.value = 0
                }
                .collect { stats ->
                    _participatingUsers.value = stats.participatingUsers
                    _userPercentage.value = stats.userPercentage
                    _averageEventsPerUser.value = stats.averageEventsPerUser
                    _totalBookings.value = stats.totalBookings
                }
        }
    }

    fun loadTrendData(days: Int) {
        viewModelScope.launch {
            getUserGrowthTrendUseCase(days)
                .catch { exception ->
                    Log.e("UserReportViewModel", "Error loading trend data: ${exception.message}", exception)
                    _totalUsersData.value = emptyList()
                    _newUsersData.value = emptyList()
                }
                .collect { trends ->
                    _totalUsersData.value = trends.totalUsers
                    _newUsersData.value = trends.newUsers
                }
        }
    }

    private fun getPreviousMonth(currentMonth: String?): String? {
        return DateUtils.getPreviousMonth(currentMonth)
    }
}