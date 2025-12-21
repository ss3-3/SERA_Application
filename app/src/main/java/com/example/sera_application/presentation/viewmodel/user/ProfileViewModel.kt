package com.example.sera_application.presentation.viewmodel.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.usecase.auth.DeleteAccountUseCase
import com.example.sera_application.domain.usecase.auth.GetCurrentUserUseCase
import com.example.sera_application.domain.usecase.auth.LogoutUseCase
import com.example.sera_application.domain.usecase.image.SaveImageUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
import com.example.sera_application.domain.usecase.user.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val saveImageUseCase: SaveImageUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    private val _isAccountDeleted = MutableStateFlow(false)
    val isAccountDeleted: StateFlow<Boolean> = _isAccountDeleted.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
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
                _error.value = e.message ?: "Failed to load profile"
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
                _error.value = e.message ?: "Logout failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount() {
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
                        _error.value = it.message ?: "Account deletion failed"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting account"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handles image selection from gallery picker.
     * Saves the image using SaveImageUseCase and updates the user profile.
     */
    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Generate unique file name for profile image
                val fileName = "user_${UUID.randomUUID()}.jpg"
                
                // Save image using use case
                val imagePath = saveImageUseCase(uri, fileName)
                
                if (imagePath != null) {
                    // Update user profile with new image path
                    val currentUser = _user.value
                    if (currentUser != null) {
                        val updatedUser = currentUser.copy(profileImagePath = imagePath)
                        val success = updateUserProfileUseCase(updatedUser)
                        if (success) {
                            _user.value = updatedUser
                        } else {
                            _error.value = "Failed to update profile picture in database"
                        }
                    }
                } else {
                    _error.value = "Failed to save image. Please try again."
                }
            } catch (e: Exception) {
                _error.value = "Image update failed: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
