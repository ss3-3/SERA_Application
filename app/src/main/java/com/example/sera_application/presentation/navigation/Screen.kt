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
    object AdminDashboard : Screen("admin_dashboard")

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
}