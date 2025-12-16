package com.example.sera_application.domain.exception

/**
 * Base exception for user-related errors.
 */
sealed class UserException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    /**
     * Thrown when a user is not found in the system.
     */
    data class UserNotFoundException(
        val userId: String,
        override val message: String = "User with ID $userId not found"
    ) : UserException(message)
    
    /**
     * Thrown when user data validation fails.
     */
    data class InvalidUserDataException(
        val field: String,
        override val message: String
    ) : UserException(message)
    
    /**
     * Thrown when a user attempts an unauthorized action.
     */
    data class UnauthorizedUserException(
        val userId: String,
        val action: String,
        override val message: String = "User $userId is not authorized to perform action: $action"
    ) : UserException(message)
    
    /**
     * Thrown when attempting to create a user that already exists.
     */
    data class UserAlreadyExistsException(
        val email: String,
        override val message: String = "User with email $email already exists"
    ) : UserException(message)
    
    /**
     * Thrown when a user account is suspended.
     */
    data class AccountSuspendedException(
        val userId: String,
        val reason: String? = null,
        override val message: String = "Account is suspended${reason?.let { ": $it" } ?: ""}"
    ) : UserException(message)
    
    /**
     * Thrown when a user account is pending approval.
     */
    data class AccountPendingApprovalException(
        val userId: String,
        override val message: String = "Account is pending approval"
    ) : UserException(message)
}
