package com.example.sera_application.presentation.navigation

import androidx.navigation.NavController
import com.example.sera_application.domain.model.enums.UserRole

/**
 * Implementation of NavigationController that uses NavController for navigation.
 * This provides a clean abstraction for navigation throughout the app.
 */
class NavigationControllerImpl(
    private val navController: NavController
) : NavigationController {

    override fun navigateToAdminDashboard() {
        navController.navigate(Screen.AdminEventManagement.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    override fun navigateToOrganizerHome() {
        navController.navigate(Screen.OrganizerEventManagement.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    override fun navigateToParticipantHome() {
        navController.navigate(Screen.EventList.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    override fun navigateToLogin() {
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Login.route) {
                inclusive = true
            }
        }
    }

    override fun navigateBack() {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }

    override fun navigateToProfile() {
        navController.navigate(Screen.Profile.route)
    }

    override fun navigateToEventList() {
        navController.navigate(Screen.EventList.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToEventDetail(eventId: String) {
        navController.navigate(Screen.EventDetails.createRoute(eventId))
    }

    override fun navigateToCreateEvent() {
        navController.navigate(Screen.CreateEvent.route)
    }

    override fun navigateToEditEvent(eventId: String) {
        navController.navigate(Screen.EditEvent.createRoute(eventId))
    }

    override fun navigateToOrganizerDashboard() {
        navController.navigate(Screen.OrganizerEventManagement.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToAdminApproval(eventId: String) {
        navController.navigate(Screen.AdminEventApproval.createRoute(eventId))
    }

    override fun navigateToAdminUserManagement() {
        navController.navigate(Screen.AdminUserManagement.route)
    }

    override fun navigateToEditUsername() {
        navController.navigate(Screen.EditUsername.route)
    }

    override fun navigateToChangePassword() {
        navController.navigate(Screen.ChangePassword.route)
    }

    override fun navigateToUserReservationHistory() {
        navController.navigate(Screen.UserReservationHistory.route)
    }

    override fun navigateToPaymentHistory() {
        navController.navigate(Screen.PaymentHistory.route)
    }

    override fun navigateToOrganizerPaymentManagement() {
        navController.navigate(Screen.OrganizerPaymentManagement.route)
    }

    override fun navigateToReservationManagement() {
        navController.navigate(Screen.ReservationManagement.route)
    }

    override fun navigateToCreateReservation(eventId: String) {
        navController.navigate(Screen.CreateReservation.createRoute(eventId))
    }

    override fun navigateToRefundRequest(paymentId: String) {
        navController.navigate(Screen.RefundRequest.createRoute(paymentId))
    }

    override fun navigateToPayment(reservationId: String) {
        navController.navigate(Screen.Payment.createRoute(reservationId))
    }

    override fun navigateToPaymentStatus(paymentId: String, success: Boolean) {
        navController.navigate(Screen.PaymentStatus.createRoute(paymentId))
    }

    override fun navigateToReceipt(paymentId: String) {
        navController.navigate(Screen.Receipt.createRoute(paymentId))
    }

    override fun navigateToReservationDetails(reservationId: String) {
        navController.navigate(Screen.ReservationDetails.createRoute(reservationId))
    }

    override fun navigateToDashboard() {
        // Navigate based on user role - this would need user context
        // For now, navigate to event list
        navController.navigate(Screen.EventList.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    override fun navigateToEventReport() {
        // TODO: Implement when report screens are created
    }

    override fun navigateToUserReport() {
        // TODO: Implement when report screens are created
    }

    override fun navigateToRevenueReport() {
        // TODO: Implement when report screens are created
    }
}
