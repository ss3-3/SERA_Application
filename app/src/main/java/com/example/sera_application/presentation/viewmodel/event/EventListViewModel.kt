package com.example.sera_application.presentation.viewmodel.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.usecase.event.GetEventListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventListUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventListUseCase: GetEventListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val allEvents = getEventListUseCase()
                val currentTime = System.currentTimeMillis()
                
                // Filter: Only show APPROVED events that haven't passed their end time
                val availableEvents = allEvents.filter { event ->
                    val isApproved = event.status == com.example.sera_application.domain.model.enums.EventStatus.APPROVED
                    val hasNotPassed = event.endTime > currentTime
                    isApproved && hasNotPassed
                }
                
                _uiState.update {
                    it.copy(
                        events = availableEvents,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load events: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
