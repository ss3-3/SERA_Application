package com.example.sera_application.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.usecase.user.ApproveOrganizerUseCase
import com.example.sera_application.domain.usecase.user.SuspendUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminUserManagementViewModel @Inject constructor(
    private val suspendUserUseCase: SuspendUserUseCase,
    private val approveOrganizerUseCase: ApproveOrganizerUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    fun suspendUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _actionSuccess.value = null
            try {
                val success = suspendUserUseCase(userId)
                if (success) {
                    _actionSuccess.value = "User suspended successfully"
                } else {
                    _error.value = "Failed to suspend user"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error suspending user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveOrganizer(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _actionSuccess.value = null
            try {
                val success = approveOrganizerUseCase(userId)
                if (success) {
                    _actionSuccess.value = "Organizer approved successfully"
                } else {
                    _error.value = "Failed to approve organizer"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error approving organizer"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
