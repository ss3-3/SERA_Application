package com.example.sera_application.presentation.viewmodel.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.usecase.event.DeleteEventUseCase
import com.example.sera_application.domain.usecase.event.GetEventsByOrganizerUseCase
import com.example.sera_application.presentation.ui.event.EventDisplayModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Organizer Event Management Screen
 */
data class OrganizerEventManagementUiState(
    val events: List<EventDisplayModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val organizerId: String = ""
)

/**
 * ViewModel for Organizer Event Management Screen
 * Shows events created by the organizer
 */
@HiltViewModel
class OrganizerEventManagementViewModel @Inject constructor(
    private val getEventsByOrganizerUseCase: GetEventsByOrganizerUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrganizerEventManagementUiState())
    val uiState: StateFlow<OrganizerEventManagementUiState> = _uiState.asStateFlow()

    /**
     * Initialize with organizer ID and load events
     */
    fun initialize(organizerId: String) {
        _uiState.update { it.copy(organizerId = organizerId) }
        loadMyEvents()
    }

    /**
     * Load events created by this organizer
     */
    fun loadMyEvents() {
        viewModelScope.launch {
            val currentOrganizerId = _uiState.value.organizerId
            if (currentOrganizerId.isBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Organizer ID not set"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val events = getEventsByOrganizerUseCase(currentOrganizerId)
                val displayModels = events.map { EventDisplayModel.fromDomain(it) }

                _uiState.update {
                    it.copy(
                        events = displayModels,
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
                    loadMyEvents() // Refresh list
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
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Get filtered events based on search
     */
    fun getFilteredEvents(): List<EventDisplayModel> {
        val state = _uiState.value
        return state.events.filter { event ->
            event.name.contains(state.searchQuery, ignoreCase = true) ||
                    event.organizer.contains(state.searchQuery, ignoreCase = true) ||
                    event.description.contains(state.searchQuery, ignoreCase = true)
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
        loadMyEvents()
    }
}