package com.example.sera_application.presentation.viewmodel.dashboard

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.uimodel.EventListUiModel
import com.example.sera_application.domain.model.uimodel.Item
import com.example.sera_application.domain.usecase.report.GetOrganizerEventsUseCase
import com.example.sera_application.domain.usecase.report.GetOrganizerStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OrganizerDashboardViewModel @Inject constructor(
    private val getOrganizerStatsUseCase: GetOrganizerStatsUseCase,
    private val getOrganizerEventsUseCase: GetOrganizerEventsUseCase
) : ViewModel() {

    private val _statsItems = MutableStateFlow<List<Item>>(emptyList())
    val statsItems: StateFlow<List<Item>> = _statsItems.asStateFlow()

    private val _eventUiList = MutableStateFlow<List<EventListUiModel>>(emptyList())
    val eventUiList: StateFlow<List<EventListUiModel>> = _eventUiList.asStateFlow()

    private val _totalParticipants = MutableStateFlow(0)
    val totalParticipants: StateFlow<Int> = _totalParticipants.asStateFlow()

    private val _averageRevenue = MutableStateFlow(0.0)
    val averageRevenue: StateFlow<Double> = _averageRevenue.asStateFlow()

    fun loadOrganizerData(organizerId: String) {
        viewModelScope.launch {
            getOrganizerStatsUseCase(organizerId).collect { stats ->
                _statsItems.value = listOf(
                    Item("Total Registered Events", stats.eventCount.toString(),
                        Color(0xFFDED8BC),
                        Color(0xFFA59217)),
                    Item("Total Revenue", String.format("%.2f", stats.totalRevenue),
                        Color(0xFFB5CAD7),
                        Color(0xFF2777A8)),
                    Item("Total Participants", stats.totalParticipants.toString(),
                        Color(0xFFEC8282),
                        Color(0xFFDB2020))
                )
                _totalParticipants.value = stats.totalParticipants
                _averageRevenue.value = stats.averageRevenue
            }
        }

        viewModelScope.launch {
            getOrganizerEventsUseCase(organizerId).collect { events ->
                _eventUiList.value = events
            }
        }
    }
}
