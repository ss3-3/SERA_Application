package com.example.sera_application.utils

import java.util.regex.Pattern

/**
 * Utility object for input validation.
 * Provides validation functions for common input fields.
 */
object InputValidator {
    
    // Validation constants
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 100
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 50
    private const val MAX_SEARCH_LENGTH = 200
    
    // Regex patterns
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+$"
    )
    
    private val USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_\\-]{3,50}$"
    )
    
    private val NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s'-]+$"
    )
    
    /**
     * Validates an email address.
     * @return Pair<Boolean, String?> where Boolean indicates validity and String is error message (if any)
     */
    fun validateEmail(email: String?): Pair<Boolean, String?> {
        return when {
            email.isNullOrBlank() -> Pair(false, "Email is required")
            !EMAIL_PATTERN.matcher(email.trim()).matches() -> Pair(false, "Invalid email format")
            email.length > 256 -> Pair(false, "Email is too long (maximum 256 characters)")
            else -> Pair(true, null)
        }
    }
    
    /**
     * Validates a username.
     * Username must be 3-50 characters, alphanumeric with underscores and hyphens allowed.
     * @return Pair<Boolean, String?> where Boolean indicates validity and String is error message (if any)
     */
    fun validateUsername(username: String?): Pair<Boolean, String?> {
        return when {
            username.isNullOrBlank() -> Pair(false, "Username is required")
            username.length < MIN_USERNAME_LENGTH -> Pair(false, "Username must be at least $MIN_USERNAME_LENGTH characters")
            username.length > MAX_USERNAME_LENGTH -> Pair(false, "Username is too long (maximum $MAX_USERNAME_LENGTH characters)")
            !USERNAME_PATTERN.matcher(username.trim()).matches() -> Pair(false, "Username can only contain letters, numbers, underscores, and hyphens")
            else -> Pair(true, null)
        }
    }
    
    /**
     * Validates a full name.
     * @return Pair<Boolean, String?> where Boolean indicates validity and String is error message (if any)
     */
    fun validateFullName(fullName: String?): Pair<Boolean, String?> {
        return when {
            fullName.isNullOrBlank() -> Pair(false, "Full name is required")
            fullName.length < MIN_NAME_LENGTH -> Pair(false, "Name must be at least $MIN_NAME_LENGTH characters")
            fullName.length > MAX_NAME_LENGTH -> Pair(false, "Name is too long (maximum $MAX_NAME_LENGTH characters)")
            !NAME_PATTERN.matcher(fullName.trim()).matches() -> Pair(false, "Name can only contain letters, spaces, hyphens, and apostrophes")
            else -> Pair(true, null)
        }
    }
    
    /**
     * Validates a search query.
     * Search query is optional but if provided, should not exceed maximum length.
     * @return Pair<Boolean, String?> where Boolean indicates validity and String is error message (if any)
     */
    fun validateSearchQuery(query: String?): Pair<Boolean, String?> {
        return when {
            query == null -> Pair(true, null) // Search is optional
            query.length > MAX_SEARCH_LENGTH -> Pair(false, "Search query is too long (maximum $MAX_SEARCH_LENGTH characters)")
            else -> Pair(true, null)
        }
    }
    
    /**
     * Simple email format check (returns Boolean only).
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return EMAIL_PATTERN.matcher(email.trim()).matches()
    }
    
    /**
     * Simple username format check (returns Boolean only).
     */
    fun isValidUsername(username: String?): Boolean {
        if (username.isNullOrBlank()) return false
        return username.length >= MIN_USERNAME_LENGTH && 
               username.length <= MAX_USERNAME_LENGTH &&
               USERNAME_PATTERN.matcher(username.trim()).matches()
    }
    
    /**
     * Checks if a string is not blank.
     */
    fun isNotBlank(value: String?): Boolean {
        return !value.isNullOrBlank()
    }
    
    /**
     * Validates minimum length.
     */
    fun hasMinLength(value: String?, minLength: Int): Boolean {
        return value?.length ?: 0 >= minLength
    }
    
    /**
     * Validates maximum length.
     */
    fun hasMaxLength(value: String?, maxLength: Int): Boolean {
        return (value?.length ?: 0) <= maxLength
    }
}
