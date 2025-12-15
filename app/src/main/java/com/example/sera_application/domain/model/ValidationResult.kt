package com.example.sera_application.domain.model

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String, val field: String? = null) : ValidationResult()
    data class MultipleErrors(val errors: List<ValidationError>) : ValidationResult()
}

data class ValidationError(
    val field: String,
    val message: String
)
