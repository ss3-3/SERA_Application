package com.example.sera_application.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.data.local.PreferencesManager
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
import com.example.sera_application.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = loginUseCase(email.trim(), password.trim())

            _loginState.value = if (result.isSuccess) {
                // Save credentials if remember me is checked
                if (rememberMe) {
                    preferencesManager.setRememberMe(true)
                    preferencesManager.saveCredentials(email.trim(), password.trim())
                } else {
                    preferencesManager.setRememberMe(false)
                    preferencesManager.clearSavedCredentials()
                }
                LoginState.Success(result.getOrNull()!!)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                LoginState.Error(errorMessage)
            }
        }
    }

    fun getSavedCredentials(): Pair<String?, String?> {
        return Pair(preferencesManager.getSavedEmail(), preferencesManager.getSavedPassword())
    }

    fun hasSavedCredentials(): Boolean {
        return preferencesManager.hasSavedCredentials()
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val result = authRepository.sendEmailVerification()

            _loginState.value = if (result.isSuccess) {
                LoginState.VerificationEmailSent
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Failed to send verification email")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _loginState.value = LoginState.Error("Please enter your email address")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = authRepository.sendPasswordResetEmail(email)

            _loginState.value = if (result.isSuccess) {
                LoginState.VerificationEmailSent // 重用这个状态表示邮件已发送
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    object VerificationEmailSent : LoginState()
    data class Error(val message: String) : LoginState()
}
