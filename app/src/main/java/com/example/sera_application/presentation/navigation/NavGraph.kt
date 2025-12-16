package com.example.sera_application.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.presentation.ui.auth.LoginScreen
import com.example.sera_application.presentation.ui.auth.SignUpScreen
import com.example.sera_application.presentation.ui.reservation.CreateReservationScreen
import com.example.sera_application.presentation.ui.reservation.MyReservationScreen
import com.example.sera_application.presentation.ui.reservation.ReservationDetailScreen
import com.example.sera_application.presentation.ui.reservation.ReservationDetailUiModel
import com.example.sera_application.presentation.ui.reservation.ReservationManagementScreen
import com.example.sera_application.presentation.ui.reservation.TicketZone
import com.example.sera_application.presentation.ui.reservation.UserReservationDetailScreen
import com.example.sera_application.presentation.ui.reservation.UserReservationDetailUiModel
import com.example.sera_application.presentation.ui.user.ChangePasswordScreen
import com.example.sera_application.presentation.ui.user.EditUsernameScreen
import com.example.sera_application.presentation.ui.user.ProfileScreen

fun NavHostController.navigateToReservationDetails(reservationId: String) {
    navigate(Screen.ReservationDetails.createRoute(reservationId))
}

@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    // Shared state for selected user role - replacing global state from navScreen.kt
    var selectedUserRole by remember { mutableStateOf(UserRole.PARTICIPANT) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password, rememberMe ->
                    // TODO: Handle actual login logic (e.g. call ViewModel)
                    
                    // For now, simulate login by setting role (could be based on email/logic)
                    selectedUserRole = UserRole.PARTICIPANT 
                    
                    // Navigate to Profile (Home replacement)
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    // TODO: Handle forgot password
                },
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onRegisterClick = { name, email, password, confirmPassword ->
                    // TODO: Handle registration logic here
                    // Navigate back to login
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Reservation Details Screen (Organizer View)
        composable(
            route = Screen.ReservationDetails.route,
            arguments = listOf(
                navArgument("reservationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
            
            // TODO: Replace with actual data fetching from ViewModel/Repository
            // For now, using sample data
            val sampleReservation = remember(reservationId) {
                ReservationDetailUiModel(
                    reservationId = reservationId,
                    participantName = "Sample User",
                    participantEmail = "sample@example.com",
                    eventName = "Sample Event",
                    venue = "Sample Venue",
                    eventDate = "1 Jan 2025",
                    eventTime = "10:00 AM",
                    seatNumbers = "A1, A2",
                    status = ReservationStatus.CONFIRMED,
                    paymentMethod = "Credit Card",
                    paymentAccount = "****-****-****-1234",
                    zoneName = "VIP Zone",
                    quantity = 2,
                    totalPrice = "RM 100.00",
                    pricePerSeat = "RM 50.00"
                )
            }
            
            ReservationDetailScreen(
                reservation = sampleReservation,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Reservation Management Screen (Organizer)
        composable(Screen.ReservationManagement.route) {
            ReservationManagementScreen(
                onBack = {
                    navController.popBackStack()
                },
                onViewReservation = { reservationId ->
                    navController.navigate(Screen.ReservationDetails.createRoute(reservationId))
                },
                onExportParticipantList = {
                    // TODO: Handle export participant list
                }
            )
        }

        // User Reservation History Screen
        composable(Screen.UserReservationHistory.route) {
            MyReservationScreen(
                onBack = {
                    navController.popBackStack()
                },
                onViewDetails = { reservation ->
                    navController.navigate("user_reservation_detail/${reservation.reservationId}")
                },
                onCancelReservation = { reservation ->
                    // TODO: Handle cancel reservation logic
                }
            )
        }

        // User Reservation Detail Screen
        composable(
            route = "user_reservation_detail/{reservationId}",
            arguments = listOf(
                navArgument("reservationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""

            // TODO: get real data from ViewModel/Repository
            val sampleReservation = UserReservationDetailUiModel(
                reservationId = reservationId,
                eventName = "Music Fiesta 6.0",
                venue = "Rimba, TARUMT",
                eventDate = "8 Nov 2025",
                eventTime = "6 PM",
                seatNumbers = "A12, A13",
                status = ReservationStatus.CONFIRMED,
                transactionDate = "29 Sep 2025",
                transactionTime = "9:28 PM",
                transactionId = "1234-1234-1234",
                paymentMethod = "PayPal",
                zoneName = "NORMAL ZONE",
                quantity = 2,
                totalPrice = "RM 70.00",
                pricePerSeat = "RM 35.00"
            )

            UserReservationDetailScreen(
                reservation = sampleReservation,
                onBack = {
                    navController.popBackStack()
                },
                onCancelReservation = {
                    // TODO: Handle cancel reservation from detail screen
                    navController.popBackStack()
                }
            )
        }

        // Edit Username Screen
        composable(Screen.EditUsername.route) {
            EditUsernameScreen(
                currentUsername = "Sample User",
                onBack = {
                    navController.popBackStack()
                },
                onConfirm = { newUsername ->
                    // TODO: Handle username update
                    navController.popBackStack()
                }
            )
        }

        // Change Password Screen
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onBack = {
                    navController.popBackStack()
                },
                onConfirm = { oldPassword, newPassword, confirmPassword ->
                    // TODO: Handle password update
                    navController.popBackStack()
                }
            )
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            val userName = when (selectedUserRole) {
                UserRole.PARTICIPANT -> "Participant1"
                UserRole.ORGANIZER -> "Organizer1"
                UserRole.ADMIN -> "Admin1"
            }

            ProfileScreen(
                userName = userName,
                userRole = selectedUserRole, 
                onBack = {
                    // If we go back from profile, we might want to exit app or go to login?
                    // Usually Profile is a top level destination.
                    // For now, let's pop back which might exit app if it's start of stack (after login replacement)
                    navController.popBackStack() 
                },
                onEditUserName = {
                    navController.navigate(Screen.EditUsername.route)
                },
                onPasswordUpdate = {
                    navController.navigate(Screen.ChangePassword.route)
                },
                onOrderHistory = {
                    navController.navigate(Screen.UserReservationHistory.route)
                },
                onPaymentHistory = {
                    // TODO: Navigate to payment history screen
                },
                onReservationManagement = {
                    navController.navigate(Screen.ReservationManagement.route)
                },
                onReport = {
                    // TODO: Navigate to report screen
                },
                onUserManagement = {
                    // TODO: Navigate to user management screen
                },
                onEventApproval = {
                    // TODO: Navigate to event approval screen
                },
                onAdminReports = {
                    // TODO: Navigate to admin reports screen
                },
                onLogout = {
                    // Navigate back to Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    // TODO: Handle delete account
                },
                onHomeClick = {
                    // Since we removed Home, maybe refresh Profile or do nothing
                },
                onAddEventClick = {
                    // TODO: Navigate to add event screen
                },
                onProfileClick = {
                    // Already on profile screen
                }
            )
        }
        
        // Create Reservation Screen
        composable(Screen.CreateReservation.route) {
            CreateReservationScreen(
                eventName = "Sample Event", // TODO: Pass real event data
                eventDate = "15 Dec 2025",
                eventTime = "7:00 PM",
                venue = "Sample Venue",
                description = "This is a sample event description.",
                zones = listOf(
                    TicketZone("VIP Zone", "RM 100.00", 50),
                    TicketZone("Normal Zone", "RM 50.00", 100)
                ),
                eventStatusLabel = "Available",
                eventStatusColor = Color(0xFF4CAF50),
                onBack = {
                    navController.popBackStack()
                },
                onPurchase = { quantities ->
                    // TODO: Handle purchase logic
                    // Navigate to payment or confirmation screen
                }
            )
        }
    }
}
