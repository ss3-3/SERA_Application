package com.example.sera_application.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
import com.example.sera_application.domain.usecase.auth.LogoutUseCase
import com.example.sera_application.domain.usecase.user.DeleteAccountUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAccountDeleted = MutableStateFlow(false)
    val isAccountDeleted: StateFlow<Boolean> = _isAccountDeleted.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedUser = getUserProfileUseCase(userId)
                _user.value = fetchedUser
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedUser = authRepository.getCurrentUser()
                _user.value = fetchedUser
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load current user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = deleteAccountUseCase(userId)
                if (success) {
                    _isAccountDeleted.value = true
                } else {
                    _error.value = "Failed to delete account"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting account"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                _isLoggedOut.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Logout failed"
            }
        }
    }
}
