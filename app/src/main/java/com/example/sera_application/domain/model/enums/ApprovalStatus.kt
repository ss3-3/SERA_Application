package com.example.sera_application.domain.model.enums

/**
 * Enum representing the approval status of an organizer account.
 */
enum class ApprovalStatus {
    /** Account is pending admin approval */
    PENDING,
    
    /** Account has been approved by admin */
    APPROVED,
    
    /** Account has been rejected by admin */
    REJECTED
}

