package com.example.sera_application.domain.util

import com.example.sera_application.domain.model.ValidationError
import com.example.sera_application.domain.model.ValidationResult
import java.util.regex.Pattern

/**
 * Utility object for validating user-related data.
 * Contains validation rules for email, phone, password, and profile information.
 */
object UserValidator {
    
    // Validation constants
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 100
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 128
    
    // Regex patterns
    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )
    
    private val PHONE_PATTERN = Pattern.compile(
        "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    )
    
    private val NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s'-]+$"
    )
    
    /**
     * Validates an email address.
     */
    fun validateEmail(email: String?): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult.Error(
                message = "Email is required",
                field = "email"
            )
            !EMAIL_PATTERN.matcher(email).matches() -> ValidationResult.Error(
                message = "Invalid email format",
                field = "email"
            )
            email.length > 256 -> ValidationResult.Error(
                message = "Email is too long (maximum 256 characters)",
                field = "email"
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates a phone number.
     * Phone is optional, but if provided, must be valid.
     */
    fun validatePhone(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) {
            return ValidationResult.Success // Phone is optional
        }
        
        return when {
            !PHONE_PATTERN.matcher(phone).matches() -> ValidationResult.Error(
                message = "Invalid phone number format",
                field = "phone"
            )
            phone.length < 8 || phone.length > 20 -> ValidationResult.Error(
                message = "Phone number must be between 8 and 20 characters",
                field = "phone"
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates a password.
     * Password must meet minimum security requirements.
     */
    fun validatePassword(password: String?): ValidationResult {
        return when {
            password.isNullOrBlank() -> ValidationResult.Error(
                message = "Password is required",
                field = "password"
            )
            password.length < MIN_PASSWORD_LENGTH -> ValidationResult.Error(
                message = "Password must be at least $MIN_PASSWORD_LENGTH characters",
                field = "password"
            )
            password.length > MAX_PASSWORD_LENGTH -> ValidationResult.Error(
                message = "Password is too long (maximum $MAX_PASSWORD_LENGTH characters)",
                field = "password"
            )
            !password.any { it.isUpperCase() } -> ValidationResult.Error(
                message = "Password must contain at least one uppercase letter",
                field = "password"
            )
            !password.any { it.isLowerCase() } -> ValidationResult.Error(
                message = "Password must contain at least one lowercase letter",
                field = "password"
            )
            !password.any { it.isDigit() } -> ValidationResult.Error(
                message = "Password must contain at least one digit",
                field = "password"
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates a full name.
     */
    fun validateFullName(fullName: String?): ValidationResult {
        return when {
            fullName.isNullOrBlank() -> ValidationResult.Error(
                message = "Full name is required",
                field = "fullName"
            )
            fullName.length < MIN_NAME_LENGTH -> ValidationResult.Error(
                message = "Name must be at least $MIN_NAME_LENGTH characters",
                field = "fullName"
            )
            fullName.length > MAX_NAME_LENGTH -> ValidationResult.Error(
                message = "Name is too long (maximum $MAX_NAME_LENGTH characters)",
                field = "fullName"
            )
            !NAME_PATTERN.matcher(fullName).matches() -> ValidationResult.Error(
                message = "Name can only contain letters, spaces, hyphens, and apostrophes",
                field = "fullName"
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates profile image path.
     */
    fun validateProfileImagePath(imagePath: String?): ValidationResult {
        if (imagePath.isNullOrBlank()) {
            return ValidationResult.Success // Image is optional
        }
        
        return when {
            imagePath.length > 500 -> ValidationResult.Error(
                message = "Image path is too long",
                field = "profileImagePath"
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates all fields for user registration.
     */
    fun validateRegistration(
        fullName: String?,
        email: String?,
        password: String?,
        phone: String?
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        (validateFullName(fullName) as? ValidationResult.Error)?.let {
            errors.add(ValidationError(it.field ?: "fullName", it.message))
        }
        
        (validateEmail(email) as? ValidationResult.Error)?.let {
            errors.add(ValidationError(it.field ?: "email", it.message))
        }
        
        (validatePassword(password) as? ValidationResult.Error)?.let {
            errors.add(ValidationError(it.field ?: "password", it.message))
        }
        
        (validatePhone(phone) as? ValidationResult.Error)?.let {
            errors.add(ValidationError(it.field ?: "phone", it.message))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.MultipleErrors(errors)
        }
    }
    
    /**
     * Validates fields for profile update.
     */
    fun validateProfileUpdate(
        fullName: String?,
        phone: String?,
        profileImagePath: String?
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Only validate if fields are provided (they're optional in updates)
        fullName?.let {
            (validateFullName(it) as? ValidationResult.Error)?.let { error ->
                errors.add(ValidationError(error.field ?: "fullName", error.message))
            }
        }
        
        phone?.let {
            (validatePhone(it) as? ValidationResult.Error)?.let { error ->
                errors.add(ValidationError(error.field ?: "phone", error.message))
            }
        }
        
        profileImagePath?.let {
            (validateProfileImagePath(it) as? ValidationResult.Error)?.let { error ->
                errors.add(ValidationError(error.field ?: "profileImagePath", error.message))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.MultipleErrors(errors)
        }
    }
}
