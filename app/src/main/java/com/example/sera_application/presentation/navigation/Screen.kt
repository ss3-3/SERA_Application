package com.example.sera_application.presentation.navigation


sealed class Screen(val route: String) {

    // Participant
    object EventList : Screen("event_list")
    object EventDetails : Screen("event_details/{eventId}") {
        fun createRoute(eventId: String) = "event_details/$eventId"
    }

    // Organizer
    object OrganizerEventManagement : Screen("organizer_events")
    object CreateEvent : Screen("create_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
    // Admin
    object AdminEventManagement : Screen("admin_events")
    object AdminEventApproval : Screen("admin_approval/{eventId}") {
        fun createRoute(eventId: String) = "admin_approval/$eventId"
    }

    object Profile : Screen("profile")
    object OrganizerHome : Screen("organizer_home")

    object AdminDashboardContainer : Screen("admin_dashboard_container")

    object OrganizerDashboard : Screen("organizer_dashboard/{organizerId}") {
        fun createRoute(organizerId: String) = "organizer_dashboard/$organizerId"
    }


    object Login : Screen("login")
    object SignUp : Screen("signup")
    object CreateReservation : Screen("create_reservation/{eventId}") {
        fun createRoute(eventId: String) = "create_reservation/$eventId"
    }


    // Reservation Navigation
    object ReservationDetails : Screen("reservation_details/{reservationId}") {
        fun createRoute(reservationId: String) = "reservation_details/$reservationId"
    }

    object EditUsername : Screen("edit_username")
    object ChangePassword : Screen("change_password")
    object EditProfile : Screen("edit_profile")
    // User Reservation History
    object UserReservationHistory : Screen("user_reservation_history")

    // Reservation Management
    object ReservationManagement : Screen("reservation_management")

    // User Navigation
    object UserList : Screen("user_list")

    // Reservation List Navigation
    object ReservationList : Screen("reservation_list/{userId}") {
        fun createRoute(userId: String) = "reservation_list/$userId"
    }

    object AdminUserManagement : Screen("admin_user_management")
    object AdminReservationManagement : Screen("admin_reservation_management")
    
    // Payment Navigation
    object PaymentHistory : Screen("payment_history")
    object OrganizerPaymentManagement : Screen("organizer_payments")
    object Payment : Screen("payment/{reservationId}") {
        fun createRoute(reservationId: String) = "payment/$reservationId"
    }
    object PaymentStatus : Screen("payment_status/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_status/$paymentId"
    }
    object Receipt : Screen("receipt/{paymentId}") {
        fun createRoute(paymentId: String) = "receipt/$paymentId"
    }
    object RefundRequest : Screen("refund_request/{paymentId}") {
        fun createRoute(paymentId: String) = "refund_request/$paymentId"
    }
    
    // Organizer Waiting Screen
    object OrganizerWaitingApproval : Screen("organizer_waiting_approval")
    
    // Notification Screen
    object Notifications : Screen("notifications/{userId}") {
        fun createRoute(userId: String) = "notifications/$userId"
    }
}