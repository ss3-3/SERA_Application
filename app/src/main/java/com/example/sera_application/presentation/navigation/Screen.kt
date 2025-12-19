package com.example.sera_application.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object CreateReservation : Screen("create_reservation/{eventId}") {
        fun createRoute(eventId: String) = "create_reservation/$eventId"
    }


    // Reservation Navigation
    object ReservationDetails : Screen("reservation_details/{reservationId}") {
        fun createRoute(reservationId: String) = "reservation_details/$reservationId"
    }

    // Profile Navigation
    object Profile : Screen("profile")
    object EditUsername : Screen("edit_username")
    object ChangePassword : Screen("change_password")
    // User Reservation History
    object UserReservationHistory : Screen("user_reservation_history")

    // Reservation Management
    object ReservationManagement : Screen("reservation_management")
    
    // Event Navigation
    object EventList : Screen("event_list")
    
    // User Navigation
    object UserList : Screen("user_list")
    
    // Reservation List Navigation
    object ReservationList : Screen("reservation_list/{userId}") {
        fun createRoute(userId: String) = "reservation_list/$userId"
    }

    object AdminUserManagement : Screen("admin_user_management")
}