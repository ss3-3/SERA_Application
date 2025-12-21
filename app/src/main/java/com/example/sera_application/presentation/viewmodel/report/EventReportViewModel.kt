package com.example.sera_application.presentation.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.uimodel.EventListUiModel
import com.example.sera_application.domain.usecase.report.FilterEventsByDateUseCase
import com.example.sera_application.domain.usecase.report.GetAllEventsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventReportViewModel(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val filterEventsByDateUseCase: FilterEventsByDateUseCase
) : ViewModel() {

    private val _events = MutableStateFlow<List<EventListUiModel>>(emptyList())
    val events: StateFlow<List<EventListUiModel>> = _events.asStateFlow()

    private val _eventCount = MutableStateFlow(0)
    val eventCount: StateFlow<Int> = _eventCount.asStateFlow()

    init {
        loadAllEvents()
    }

    private fun loadAllEvents() {
        viewModelScope.launch {
            getAllEventsUseCase().collect { eventList ->
                _events.value = eventList
                _eventCount.value = eventList.size
            }
        }
    }

    fun filterByDateRange(startDate: Long?, endDate: Long?) {
        viewModelScope.launch {
            filterEventsByDateUseCase(startDate, endDate).collect { filtered ->
                _events.value = filtered
                _eventCount.value = filtered.size
            }
        }
    }
}