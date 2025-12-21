package com.example.sera_application.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.usecase.user.ApproveOrganizerUseCase
import com.example.sera_application.domain.usecase.user.GetPendingOrganizersUseCase
import com.example.sera_application.domain.usecase.user.RejectOrganizerUseCase
import com.example.sera_application.domain.usecase.user.SuspendUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Admin User Management Screen
 */
data class AdminUserManagementUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminUserManagementViewModel @Inject constructor(
    private val getPendingOrganizersUseCase: GetPendingOrganizersUseCase,
    private val suspendUserUseCase: SuspendUserUseCase,
    private val approveOrganizerUseCase: ApproveOrganizerUseCase,
    private val rejectOrganizerUseCase: RejectOrganizerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserManagementUiState())
    val uiState: StateFlow<AdminUserManagementUiState> = _uiState.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val users = getPendingOrganizersUseCase()
                _uiState.update { it.copy(users = users, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load pending organizers"
                    ) 
                }
            }
        }
    }

    fun suspendUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _actionSuccess.value = null
            try {
                val success = suspendUserUseCase(userId)
                if (success) {
                    _actionSuccess.value = "User suspended successfully"
                    loadUsers() // Refresh user list
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to suspend user") }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error suspending user"
                    ) 
                }
            }
        }
    }

    fun approveOrganizer(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _actionSuccess.value = null
            try {
                val success = approveOrganizerUseCase(userId)
                if (success) {
                    _actionSuccess.value = "Organizer approved successfully"
                    loadUsers() // Refresh user list
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to approve organizer") }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error approving organizer"
                    ) 
                }
            }
        }
    }

    fun rejectOrganizer(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _actionSuccess.value = null
            try {
                val success = rejectOrganizerUseCase(userId)
                if (success) {
                    _actionSuccess.value = "Organizer rejected successfully"
                    loadUsers() // Refresh user list
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to reject organizer") }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error rejecting organizer"
                    ) 
                }
            }
        }
    }
}