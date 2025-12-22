package com.example.sera_application.presentation.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.uimodel.EventListUiModel
import com.example.sera_application.domain.usecase.report.FilterEventsByDateUseCase
import com.example.sera_application.domain.usecase.report.GetAllEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import android.util.Log

@HiltViewModel
class EventReportViewModel @Inject constructor(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val filterEventsByDateUseCase: FilterEventsByDateUseCase
) : ViewModel() {

    private val _events = MutableStateFlow<List<EventListUiModel>>(emptyList())
    val events: StateFlow<List<EventListUiModel>> = _events.asStateFlow()

    private val _eventCount = MutableStateFlow(0)
    val eventCount: StateFlow<Int> = _eventCount.asStateFlow()

    init {
        try {
            Log.d("EventReportViewModel", "EventReportViewModel initialized")
        } catch (e: Exception) {
            Log.e("EventReportViewModel", "Error during initialization: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun loadAllEvents() {
        viewModelScope.launch {
            getAllEventsUseCase()
                .catch { exception ->
                    Log.e("EventReportViewModel", "Error loading events: ${exception.message}", exception)
                    _events.value = emptyList()
                    _eventCount.value = 0
                }
                .collect { eventList ->
                    _events.value = eventList
                    _eventCount.value = eventList.size
                }
        }
    }

    fun filterByDateRange(startDate: Long?, endDate: Long?) {
        viewModelScope.launch {
            filterEventsByDateUseCase(startDate, endDate)
                .catch { exception ->
                    Log.e("EventReportViewModel", "Error filtering events: ${exception.message}", exception)
                    _events.value = emptyList()
                    _eventCount.value = 0
                }
                .collect { filtered ->
                    _events.value = filtered
                    _eventCount.value = filtered.size
                }
        }
    }
}