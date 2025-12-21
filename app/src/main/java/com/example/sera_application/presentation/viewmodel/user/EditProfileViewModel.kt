package com.example.sera_application.presentation.viewmodel.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
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
class EditProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val authRepository: AuthRepository,
    private val saveImageUseCase: SaveImageUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _passwordUpdateSuccess = MutableStateFlow(false)
    val passwordUpdateSuccess: StateFlow<Boolean> = _passwordUpdateSuccess.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedUser = getUserProfileUseCase(userId)
                _user.value = fetchedUser
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user for editing"
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
                if (fetchedUser != null) {
                    _user.value = fetchedUser
                } else {
                    _error.value = "Failed to load current user: User not authenticated"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load current user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _updateSuccess.value = false
            try {
                val success = updateUserProfileUseCase(updatedUser)
                if (success) {
                    _user.value = updatedUser
                    _updateSuccess.value = true
                } else {
                    _error.value = "Failed to update profile"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _passwordUpdateSuccess.value = false
            try {
                val success = authRepository.updatePassword(currentPassword, newPassword)
                if (success) {
                    _passwordUpdateSuccess.value = true
                } else {
                    _error.value = "Failed to update password"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating password"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Handles image selection from gallery picker.
     * Saves the image using SaveImageUseCase and returns the image path.
     * 
     * @param uri The Uri of the selected image
     * @param onSuccess Callback with the saved image path
     * @param onError Callback with error message
     */
    fun onImageSelected(uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Generate unique file name for profile image
                val fileName = "user_${UUID.randomUUID()}.jpg"
                
                // Save image using use case
                val imagePath = saveImageUseCase(uri, fileName)
                
                if (imagePath != null) {
                    onSuccess(imagePath)
                } else {
                    val errorMsg = "Failed to save image. Please try again."
                    _error.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Image save failed: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }
}