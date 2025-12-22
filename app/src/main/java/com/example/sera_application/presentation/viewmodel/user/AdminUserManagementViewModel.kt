package com.example.sera_application.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.usecase.auth.GetCurrentUserUseCase
import com.example.sera_application.domain.usecase.user.ActivateUserUseCase
import com.example.sera_application.domain.usecase.user.ApproveOrganizerUseCase
import com.example.sera_application.domain.usecase.user.GetAllUsersUseCase
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
    val allUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterType: UserFilterType = UserFilterType.ALL,
    val searchQuery: String = ""
)

enum class UserFilterType {
    ALL,
    PARTICIPANT,
    APPROVED_ORGANIZER,
    PENDING_ORGANIZER,
    SUSPENDED
}

@HiltViewModel
class AdminUserManagementViewModel @Inject constructor(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val getPendingOrganizersUseCase: GetPendingOrganizersUseCase,
    private val suspendUserUseCase: SuspendUserUseCase,
    private val activateUserUseCase: ActivateUserUseCase,
    private val approveOrganizerUseCase: ApproveOrganizerUseCase,
    private val rejectOrganizerUseCase: RejectOrganizerUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
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
                // Get current admin user to exclude
                val currentAdmin = getCurrentUserUseCase()
                val currentAdminId = currentAdmin?.userId ?: ""
                
                // Get all users and exclude the admin account itself
                val allUsers = getAllUsersUseCase()
                val filteredUsers = allUsers.filter { it.userId != currentAdminId }
                
                _uiState.update { it.copy(allUsers = filteredUsers, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load users"
                    ) 
                }
            }
        }
    }
    
    fun updateFilter(filterType: UserFilterType) {
        _uiState.update { it.copy(filterType = filterType) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun getFilteredUsers(): List<User> {
        val state = _uiState.value
        val filteredByType = when (state.filterType) {
            UserFilterType.ALL -> state.allUsers
            UserFilterType.PARTICIPANT -> state.allUsers.filter { it.role == UserRole.PARTICIPANT }
            UserFilterType.APPROVED_ORGANIZER -> state.allUsers.filter { 
                // Only show approved organizers that are NOT suspended
                it.role == UserRole.ORGANIZER && 
                it.isApproved && 
                it.approvalStatus == com.example.sera_application.domain.model.enums.ApprovalStatus.APPROVED &&
                it.accountStatus != "SUSPENDED"
            }
            UserFilterType.PENDING_ORGANIZER -> state.allUsers.filter { 
                // Only show pending (not approved) organizers that are NOT suspended
                it.role == UserRole.ORGANIZER && 
                it.accountStatus != "SUSPENDED" &&
                (it.approvalStatus == com.example.sera_application.domain.model.enums.ApprovalStatus.PENDING ||
                 (!it.isApproved && it.approvalStatus != com.example.sera_application.domain.model.enums.ApprovalStatus.APPROVED))
            }
            UserFilterType.SUSPENDED -> state.allUsers.filter { 
                // Show all suspended users regardless of role or approval status
                it.accountStatus == "SUSPENDED"
            }
        }

        // Apply search filter
        return if (state.searchQuery.isEmpty()) {
            filteredByType
        } else {
            filteredByType.filter { user ->
                user.fullName.contains(state.searchQuery, ignoreCase = true) ||
                user.email.contains(state.searchQuery, ignoreCase = true) ||
                user.userId.contains(state.searchQuery, ignoreCase = true)
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

    fun activateUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _actionSuccess.value = null
            try {
                val success = activateUserUseCase(userId)
                if (success) {
                    _actionSuccess.value = "User activated successfully"
                    loadUsers() // Refresh user list
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to activate user") }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error activating user"
                    ) 
                }
            }
        }
    }
    
    fun removeUser(userId: String) {
        // This could be implemented as delete or permanent removal
        // For now, we'll use rejectOrganizer
        rejectOrganizer(userId)
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