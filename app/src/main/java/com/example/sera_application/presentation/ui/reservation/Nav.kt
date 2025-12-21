package com.example.sera_application.presentation.ui.reservation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.presentation.navigation.Screen
import com.example.sera_application.presentation.ui.auth.LoginScreen
import com.example.sera_application.presentation.ui.auth.SignUpScreen
import com.example.sera_application.presentation.ui.user.ProfileScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier


){
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Home Page Screen (navScreen.kt)
        composable("home") {
            HomePageScreen(
                navController = navController
            )
        }


        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password, rememberMe ->
                    // Navigate back to home after login
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
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


        // SignUp Screen
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onRegisterClick = { name, email, password, confirmPassword ->
                    // Navigate back to login
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
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
                    navController.navigate(Screen.UserReservationDetails.createRoute(reservation.reservationId))
                },
                onCancelReservation = { reservation ->
                    // TODO: Handle cancel reservation
                }
            )
        }

        // User Reservation Details Screen
        composable(
            route = Screen.UserReservationDetails.route,
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
                UserReservationDetailUiModel(
                    reservationId = reservationId,
                    eventName = "Sample Event",
                    venue = "Sample Venue",
                    eventDate = "1 Jan 2025",
                    eventTime = "10:00 AM",
                    seatNumbers = "A1, A2",
                    status = ReservationStatus.CONFIRMED,
                    transactionDate = "1 Jan 2025",
                    transactionTime = "9:00 AM",
                    transactionId = "TXN-$reservationId",
                    paymentMethod = "Credit Card",
                    zoneName = "VIP Zone",
                    quantity = 2,
                    totalPrice = "RM 100.00",
                    pricePerSeat = "RM 50.00",
                    qrCodeData = reservationId
                )
            }
            
            UserReservationDetailScreen(
                reservation = sampleReservation,
                onBack = {
                    navController.popBackStack()
                },
                onCancelReservation = {
                    // TODO: Handle cancel reservation
                }
            )
        }

        // Reservation Management Screen
        composable(Screen.ReservationManagement.route) {
            ReservationManagementScreen(
                onBack = {
                    navController.popBackStack()
                },
                onViewReservation = { reservationId ->
                    // TODO: Navigate to reservation details
                },
                onExportParticipantList = {
                    // TODO: Handle export participant list
                }
            )
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            val currentRole = selectedUserRole.value
            val userName = when (currentRole) {
                UserRole.PARTICIPANT -> "Participant1"
                UserRole.ORGANIZER -> "Organizer1"
                UserRole.ADMIN -> "Admin1"
            }


            ProfileScreen(
                userName = userName,
                userRole = currentRole,
                onBack = {
                    navController.popBackStack()
                },
                onEditUserName = {
                    // TODO: Navigate to edit user name screen
                },
                onPasswordUpdate = {
                    // TODO: Navigate to password update screen
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
                    navController.navigate(Screen.Login.route) {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    // TODO: Handle delete account
                },
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onAddEventClick = {
                    // TODO: Navigate to add event screen
                },
                onProfileClick = {
                    // Already on profile screen
                }
            )
        }
    }
}
