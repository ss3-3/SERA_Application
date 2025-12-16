package com.example.sera_application.domain.model.enums

/**
 * Enum representing the status of a user account.
 */
enum class AccountStatus {
    /** Account is active and can be used normally */
    ACTIVE,
    
    /** Account is suspended and cannot perform actions */
    SUSPENDED,
    
    /** Account is pending approval (for organizers) */
    PENDING_APPROVAL,
    
    /** Account has been deactivated by the user */
    DEACTIVATED
}
