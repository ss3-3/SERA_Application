package com.example.sera_application.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//data class UserUiState(
//    val currentUser: User? = null,
//    val isLoading: Boolean = false,
//    val error: String? = null
//)
//
//@HiltViewModel
//class UserViewModel @Inject constructor(
//    private val userRepository: UserRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(UserUiState())
//    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
//
//    // 加载当前用户信息
//    fun loadUser(userId: String) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            try {
//                val user = userRepository.getUserById(userId)
//                _uiState.update {
//                    it.copy(
//                        currentUser = user,
//                        isLoading = false,
//                        error = null
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = e.message ?: "Unknown error"
//                    )
//                }
//            }
//        }
//    }
//
//    // 更新用户名
//    fun updateUsername(
//        userId: String,
//        newUsername: String,
//        onSuccess: () -> Unit,
//        onError: (String) -> Unit
//    ) {
//        viewModelScope.launch {
//            try {
//                val success = userRepository.updateUsername(userId, newUsername)
//                if (success) {
//                    // 重新加载用户信息
//                    loadUser(userId)
//                    onSuccess()
//                } else {
//                    onError("Failed to update username")
//                }
//            } catch (e: Exception) {
//                onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    // 更新密码
//    fun updatePassword(
//        userId: String,
//        oldPassword: String,
//        newPassword: String,
//        onSuccess: () -> Unit,
//        onError: (String) -> Unit
//    ) {
//        viewModelScope.launch {
//            try {
//                val success = userRepository.updatePassword(userId, oldPassword, newPassword)
//                if (success) {
//                    onSuccess()
//                } else {
//                    onError("Failed to update password")
//                }
//            } catch (e: Exception) {
//                onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    // 登录
//    fun login(
//        email: String,
//        password: String,
//        onSuccess: (User) -> Unit,
//        onError: (String) -> Unit
//    ) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            try {
//                val user = userRepository.getUserByEmail(email)
//                if (user != null) {
//                    // TODO: 验证密码
//                    _uiState.update {
//                        it.copy(
//                            currentUser = user,
//                            isLoading = false,
//                            error = null
//                        )
//                    }
//                    onSuccess(user)
//                } else {
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            error = "User not found"
//                        )
//                    }
//                    onError("User not found")
//                }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = e.message ?: "Unknown error"
//                    )
//                }
//                onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//}