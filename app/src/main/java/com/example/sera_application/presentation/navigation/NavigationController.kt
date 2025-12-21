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
    fun navigateToAdminApproval(eventId: String)
    fun navigateToAdminUserManagement()
    fun navigateToEditUsername()
    fun navigateToChangePassword()
    fun navigateToUserReservationHistory()
    fun navigateToPaymentHistory()
    fun navigateToOrganizerPaymentManagement()
    fun navigateToReservationManagement()
    fun navigateToCreateReservation(eventId: String)
    fun navigateToRefundRequest(paymentId: String)

    // Payment Navigation
    fun navigateToPayment(reservationId: String)
    fun navigateToPaymentStatus(paymentId: String, success: Boolean)
    fun navigateToReceipt(paymentId: String)

    // Reservation Navigation
    fun navigateToReservationDetails(reservationId: String)

    // Dashboard Navigation
    fun navigateToDashboard()

    // Report Navigation
    fun navigateToEventReport()
    fun navigateToUserReport()
    fun navigateToRevenueReport()
}