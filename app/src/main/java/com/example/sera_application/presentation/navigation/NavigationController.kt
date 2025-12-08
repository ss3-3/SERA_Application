package com.example.sera_application.presentation.navigation

/**
 * Defines all navigation routes in the application
 */

interface NavigationController {
    // User Management Navigation
    fun navigateToAdminDashboard()
    fun navigateToOrganizerHome()
    fun navigateToParticipantHome()
    fun navigateToLogin()
    fun navigateBack()
    fun navigateToProfile()

    // Event Management Navigation
    fun navigateToEventList()
    fun navigateToEventDetail(eventId: String)
    fun navigateToCreateEvent()
    fun navigateToEditEvent(eventId: String)
    fun navigateToOrganizerDashboard()

    // Admin Functions Navigation
    fun navigateToAdminApproval()

    // Payment Navigation
    fun navigateToPayment(reservationId: String)
    fun navigateToPaymentStatus(paymentId: String, success: Boolean)
    fun navigateToReceipt(paymentId: String)

    // Dashboard Navigation
    fun navigateToDashboard()

    // Report Navigation
    fun navigateToEventReport()
    fun navigateToUserReport()
    fun navigateToRevenueReport()
}