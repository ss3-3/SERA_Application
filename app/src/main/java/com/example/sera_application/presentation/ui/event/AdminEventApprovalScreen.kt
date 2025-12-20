package com.example.sera_application.presentation.viewmodel.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.usecase.event.ApproveEventUseCase
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.event.RejectEventUseCase
import com.example.sera_application.presentation.ui.event.AdminEventDetails
import com.example.sera_application.utils.DateTimeFormatterUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Admin Event Approval Screen
 */
data class AdminEventApprovalUiState(
    val event: AdminEventDetails? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isApprovalSuccess: Boolean = false,
    val isRejectionSuccess: Boolean = false
)

/**
 * ViewModel for Admin Event Approval Screen
 */
@HiltViewModel
class AdminEventApprovalViewModel @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val approveEventUseCase: ApproveEventUseCase,
    private val rejectEventUseCase: RejectEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminEventApprovalUiState())
    val uiState: StateFlow<AdminEventApprovalUiState> = _uiState.asStateFlow()

    /**
     * Load event details by ID
     */
    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val event = getEventByIdUseCase(eventId)
                if (event != null) {
                    val adminDetails = AdminEventDetails(
                        id = event.eventId,
                        name = event.name,
                        description = event.description,
                        rockZoneSeats = event.rockZoneSeats.toString(),
                        normalZoneSeats = event.normalZoneSeats.toString(),
                        date = event.date,
                        startTime = event.startTime,
                        endTime = event.endTime,
                        venue = event.location,
                        organizer = event.organizerName,
                        bannerUrl = event.imagePath
                    )
                    _uiState.update {
                        it.copy(
                            event = adminDetails,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Event not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load event: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Approve an event
     */
    fun approveEvent(eventId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val success = approveEventUseCase(eventId)
                if (success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isApprovalSuccess = true,
                            errorMessage = null
                        )
                    }
                    onResult(true, null)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to approve event"
                        )
                    }
                    onResult(false, "Failed to approve event")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error approving event: ${e.message}"
                    )
                }
                onResult(false, e.message)
            }
        }
    }

    /**
     * Reject an event
     */
    fun rejectEvent(eventId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val success = rejectEventUseCase(eventId)
                if (success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRejectionSuccess = true,
                            errorMessage = null
                        )
                    }
                    onResult(true, null)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to reject event"
                        )
                    }
                    onResult(false, "Failed to reject event")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error rejecting event: ${e.message}"
                    )
                }
                onResult(false, e.message)
            }
        }
    }

    /**
     * Reset success states
     */
    fun resetSuccessStates() {
        _uiState.update {
            it.copy(isApprovalSuccess = false, isRejectionSuccess = false)
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}