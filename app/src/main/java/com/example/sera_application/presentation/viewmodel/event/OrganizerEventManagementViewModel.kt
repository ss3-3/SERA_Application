package com.example.sera_application.presentation.viewmodel.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.usecase.event.DeleteEventUseCase
import com.example.sera_application.domain.usecase.event.GetApprovedEventsUseCase
import com.example.sera_application.domain.usecase.event.GetEventsByOrganizerUseCase
import com.example.sera_application.presentation.ui.event.EventDisplayModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async

/**
 * UI State for Organizer Event Management Screen
 */
data class OrganizerEventManagementUiState(
    val bannerEvents: List<EventDisplayModel> = emptyList(),
    val myEvents: List<EventDisplayModel> = emptyList(), // Renamed for clarity
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

/**
 * ViewModel for Organizer Event Management Screen
 * Shows events created by the organizer
 */
@HiltViewModel
class OrganizerEventManagementViewModel @Inject constructor(
    private val getEventsByOrganizerUseCase: GetEventsByOrganizerUseCase,
    private val getApprovedEventsUseCase: GetApprovedEventsUseCase, private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(OrganizerEventManagementUiState())
    val uiState: StateFlow<OrganizerEventManagementUiState> = _uiState.asStateFlow()

    /**
     * Load both banner events and organizer-specific events concurrently.
     */
    fun loadAllOrganizerData() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "User not logged in") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Launch both requests concurrently
                val bannerEventsDeferred = async { getApprovedEventsUseCase() }
                val myEventsDeferred = async { getEventsByOrganizerUseCase(currentUser.uid) }

                // Await results
                val bannerEvents = bannerEventsDeferred.await()
                val myEvents = myEventsDeferred.await()

                // Update state once with all data
                _uiState.update {
                    it.copy(
                        bannerEvents = bannerEvents.map(EventDisplayModel::fromDomain),
                        myEvents = myEvents.map(EventDisplayModel::fromDomain),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("OrganizerVM", "Failed to load organizer data", e)
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
     * Delete an event and refresh the list.
     */
    fun deleteEvent(eventId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val success = deleteEventUseCase(eventId)
                if (success) {
                    _uiState.update { it.copy(isLoading = false) }
                    loadAllOrganizerData() // Refresh all data
                    onResult(true, null)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to delete event") }
                    onResult(false, "Failed to delete event")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error deleting event: ${e.message}") }
                onResult(false, e.message)
            }
        }
    }

    /**
     * Update search query.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Get filtered events based on search.
     */
    fun getFilteredEvents(): List<EventDisplayModel> {
        val state = _uiState.value
        return state.myEvents.filter { event ->
            event.name.contains(state.searchQuery, ignoreCase = true) ||
                    event.description.contains(state.searchQuery, ignoreCase = true)
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Refresh events list.
     */
    fun refreshEvents() {
        loadAllOrganizerData()
    }
}
