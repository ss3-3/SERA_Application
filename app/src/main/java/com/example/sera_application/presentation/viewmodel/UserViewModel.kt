package com.example.sera_application.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
import com.example.sera_application.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Get current authenticated user from AuthRepository
                 val user = authRepository.getCurrentUser()
                
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoading = false,
                        error = if (user == null) "User not found" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun login(email: String, password: String, onSuccess: (User) -> Unit, onError: (String) -> Unit) {
         viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Use runCatching to handle potential exceptions from repo
                val result = authRepository.login(email, password)
                
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                             it.copy(
                                currentUser = user,
                                isLoading = false,
                                error = null
                            )
                        }
                        onSuccess(user)
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Login failed"
                            )
                        }
                        onError(e.message ?: "Login failed")
                    }
                )
            } catch (e: Exception) {
                 _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                onError(e.message ?: "Unknown error")
            }
         }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authRepository.logout()
                _uiState.update { UserUiState() } // Reset state
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Logout failed")
                }
            }
        }
    }

    fun updateUsername(userId: String, newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Fetch latest user data to ensure we have the full object
                val currentUser = userRepository.getUserById(userId) ?: _uiState.value.currentUser
                
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(fullName = newName, updatedAt = System.currentTimeMillis())
                    val success = userRepository.updateUser(updatedUser)
                    
                    if (success) {
                        _uiState.update {
                            it.copy(
                                currentUser = updatedUser,
                                isLoading = false,
                                error = null
                            )
                        }
                        onSuccess()
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to update profile") }
                        onError("Failed to update profile")
                    }
                } else {
                     _uiState.update { it.copy(isLoading = false, error = "User not found") }
                     onError("User not found")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Update failed")
                }
                onError(e.message ?: "Update failed")
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val success = authRepository.updatePassword(currentPassword, newPassword)
                if (success) {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to update password") }
                    onError("Failed to update password")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Update password failed")
                }
                onError(e.message ?: "Update password failed")
            }
        }
    }
}
