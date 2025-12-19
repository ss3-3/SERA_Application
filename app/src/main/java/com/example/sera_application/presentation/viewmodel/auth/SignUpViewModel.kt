package com.example.sera_application.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
import com.example.sera_application.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()

    fun register(name: String, email: String, password: String, role: String = "PARTICIPANT") {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading

            val result = registerUseCase(name, email, password, role)

            _signUpState.value = if (result.isSuccess) {
                SignUpState.Success(result.getOrNull()!!)
            } else {
                SignUpState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading

            val result = authRepository.sendEmailVerification()

            _signUpState.value = if (result.isSuccess) {
                SignUpState.VerificationEmailSent
            } else {
                SignUpState.Error(result.exceptionOrNull()?.message ?: "Failed to send verification email")
            }
        }
    }

    fun resetState() {
        _signUpState.value = SignUpState.Idle
    }
}

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val user: User) : SignUpState()
    object VerificationEmailSent : SignUpState()
    data class Error(val message: String) : SignUpState()
}