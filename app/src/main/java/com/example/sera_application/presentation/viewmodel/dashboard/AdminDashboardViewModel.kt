package com.example.sera_application.presentation.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.uimodel.Item
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.usecase.report.GetAdminStatsUseCase
import com.example.sera_application.domain.usecase.report.GetPopularEventsUseCase
import com.example.sera_application.domain.usecase.report.GetTrendDataUseCase
import com.patrykandpatryk.vico.core.entry.FloatEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import android.util.Log

data class PopularEventUiModel(
    val eventId: String,
    val title: String,
    val participants: Int,
    val imagePath: String?,
    val rank: Int
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val getAdminStatsUseCase: GetAdminStatsUseCase,
    private val getTrendDataUseCase: GetTrendDataUseCase,
    private val getPopularEventsUseCase: GetPopularEventsUseCase,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _statsItems = MutableStateFlow<List<Item>>(emptyList())
    val statsItems: StateFlow<List<Item>> = _statsItems.asStateFlow()

    private val _bookingData = MutableStateFlow<List<FloatEntry>>(emptyList())
    val bookingData: StateFlow<List<FloatEntry>> = _bookingData.asStateFlow()

    private val _userGrowthData = MutableStateFlow<List<FloatEntry>>(emptyList())
    val userGrowthData: StateFlow<List<FloatEntry>> = _userGrowthData.asStateFlow()

    private val _popularEvents = MutableStateFlow<List<PopularEventUiModel>>(emptyList())
    val popularEvents: StateFlow<List<PopularEventUiModel>> = _popularEvents.asStateFlow()

    init {
        try {
            Log.d("AdminDashboardViewModel", "Initializing AdminDashboardViewModel")
            loadDashboardData()
        } catch (e: Exception) {
            Log.e("AdminDashboardViewModel", "Error during initialization: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                getAdminStatsUseCase()
                    .catch { exception ->
                        Log.e("AdminDashboardViewModel", "Error loading admin stats: ${exception.message}", exception)
                        _statsItems.value = emptyList()
                    }
                    .collect { stats ->
                        _statsItems.value = stats
                    }
            } catch (e: Exception) {
                Log.e("AdminDashboardViewModel", "Exception in loadDashboardData (stats): ${e.message}", e)
                _statsItems.value = emptyList()
            }
        }

        viewModelScope.launch {
            try {
                getTrendDataUseCase()
                    .catch { exception ->
                        Log.e("AdminDashboardViewModel", "Error loading trend data: ${exception.message}", exception)
                        _bookingData.value = emptyList()
                        _userGrowthData.value = emptyList()
                    }
                    .collect { trends ->
                        _bookingData.value = trends.bookings
                        _userGrowthData.value = trends.users
                    }
            } catch (e: Exception) {
                Log.e("AdminDashboardViewModel", "Exception in loadDashboardData (trends): ${e.message}", e)
                _bookingData.value = emptyList()
                _userGrowthData.value = emptyList()
            }
        }

        viewModelScope.launch {
            try {
                val events = getPopularEventsUseCase(limit = 3)
                val popularEventsWithParticipants = events.mapIndexed { index, event ->
                    val participants = try {
                        reservationRepository.getParticipantsByEvent(event.eventId)
                    } catch (e: Exception) {
                        Log.e("AdminDashboardViewModel", "Error getting participants for event ${event.eventId}: ${e.message}", e)
                        0
                    }
                    PopularEventUiModel(
                        eventId = event.eventId,
                        title = event.name,
                        participants = participants,
                        imagePath = event.imagePath,
                        rank = index + 1
                    )
                }
                _popularEvents.value = popularEventsWithParticipants
            } catch (e: Exception) {
                Log.e("AdminDashboardViewModel", "Exception in loadDashboardData (popular events): ${e.message}", e)
                _popularEvents.value = emptyList()
            }
        }
    }
}