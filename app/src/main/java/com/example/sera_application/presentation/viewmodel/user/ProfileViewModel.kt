package com.example.sera_application.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.usecase.auth.DeleteAccountUseCase
import com.example.sera_application.domain.usecase.auth.GetCurrentUserUseCase
import com.example.sera_application.domain.usecase.auth.LogoutUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    private val _isAccountDeleted = MutableStateFlow(false)
    val isAccountDeleted: StateFlow<Boolean> = _isAccountDeleted.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser != null) {
                    // Load full profile details
                    val fullProfile = getUserProfileUseCase(currentUser.userId)
                    _user.value = fullProfile ?: currentUser
                } else {
                    _user.value = null
                }
            } catch (e: Exception) {
                _user.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = logoutUseCase()
                if (success) {
                    _user.value = null
                    _isLoggedOut.value = true
                }
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = deleteAccountUseCase()
                result.fold(
                    onSuccess = {
                        _user.value = null
                        _isAccountDeleted.value = true
                    },
                    onFailure = {
                        // Handle error if needed
                    }
                )
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }
}
