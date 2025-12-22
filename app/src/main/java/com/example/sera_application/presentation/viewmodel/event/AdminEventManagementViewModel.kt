package com.example.sera_application.presentation.viewmodel.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.enums.EventStatus
import com.example.sera_application.domain.usecase.event.DeleteEventUseCase
import com.example.sera_application.domain.usecase.event.GetEventListUseCase
import com.example.sera_application.presentation.ui.event.AdminEventModel
import com.example.sera_application.utils.DateTimeFormatterUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Admin Event Management Screen
 */
data class AdminEventManagementUiState(
    val events: List<AdminEventModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedStatus: EventStatus? = null,
    val searchQuery: String = ""
)

/**
 * ViewModel for Admin Event Management Screen
 * Shows all events with status filtering
 */
@HiltViewModel
class AdminEventManagementViewModel @Inject constructor(
    private val getEventListUseCase: GetEventListUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminEventManagementUiState())
    val uiState: StateFlow<AdminEventManagementUiState> = _uiState.asStateFlow()

    init {
        loadAllEvents()
    }

    /**
     * Load all events (all statuses for admin)
     */
    fun loadAllEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val events = getEventListUseCase()
                val today = System.currentTimeMillis()
                
                val adminModels = events.map { event ->
                    // Determine display status: if event date has passed, show as COMPLETED
                    // Exception: CANCELLED events remain CANCELLED even if date passed
                    val displayStatus = if (event.date < today && event.status != EventStatus.CANCELLED) {
                        EventStatus.COMPLETED
                    } else {
                        event.status
                    }
                    
                    AdminEventModel(
                        id = event.eventId,
                        name = event.name,
                        date = event.date,
                        startTime = event.startTime,
                        endTime = event.endTime,
                        status = displayStatus, // Use calculated display status
                        bannerUrl = event.imagePath
                    )
                }
                _uiState.update {
                    it.copy(
                        events = adminModels,
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

    /**
     * Delete an event
     */
    fun deleteEvent(eventId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val success = deleteEventUseCase(eventId)
                if (success) {
                    _uiState.update { it.copy(isLoading = false) }
                    loadAllEvents() // Refresh list
                    onResult(true, null)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to delete event"
                        )
                    }
                    onResult(false, "Failed to delete event")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error deleting event: ${e.message}"
                    )
                }
                onResult(false, e.message)
            }
        }
    }

    /**
     * Update status filter
     */
    fun updateStatusFilter(status: EventStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Get filtered events based on status and search
     */
    fun getFilteredEvents(): List<AdminEventModel> {
        val state = _uiState.value
        return state.events.filter { event ->
            val matchesStatus = state.selectedStatus == null || event.status == state.selectedStatus
            val matchesSearch = event.name.contains(state.searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Refresh events list
     */
    fun refreshEvents() {
        loadAllEvents()
    }
}











