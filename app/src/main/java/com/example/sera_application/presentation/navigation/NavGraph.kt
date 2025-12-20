package com.example.sera_application.presentation.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import com.example.sera_application.presentation.ui.user.UserListScreen
import com.example.sera_application.presentation.ui.reservation.ReservationListScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.presentation.ui.auth.LoginScreen
import com.example.sera_application.presentation.ui.auth.SignUpScreen
import com.example.sera_application.presentation.ui.event.EventDetailsScreen
import com.example.sera_application.presentation.ui.event.OrganizerEventManagementScreen
import com.example.sera_application.presentation.ui.reservation.CreateReservationScreen
import com.example.sera_application.presentation.ui.reservation.MyReservationScreen
import com.example.sera_application.presentation.ui.reservation.ReservationDetailScreen
import com.example.sera_application.presentation.ui.reservation.ReservationManagementScreen
import com.example.sera_application.presentation.ui.reservation.UserReservationDetailScreen
import com.example.sera_application.presentation.ui.user.ChangePasswordScreen
import com.example.sera_application.presentation.ui.user.EditUsernameScreen
import com.example.sera_application.presentation.ui.user.ProfileScreen
import com.example.sera_application.presentation.ui.event.*
import com.example.sera_application.presentation.ui.payment.*
import com.example.sera_application.presentation.viewmodel.event.EventFormViewModel

fun NavHostController.navigateToReservationDetails(reservationId: String) {
    navigate(Screen.ReservationDetails.createRoute(reservationId))
}

@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    // Determine the home screen based on the user's role
                    val homeScreen = when (user.role) {
                        UserRole.ORGANIZER -> Screen.OrganizerEventManagement.route
                        UserRole.ADMIN -> Screen.AdminEventManagement.route
                        else -> Screen.EventList.route // Default for PARTICIPANT
                    }

                    // Navigate to the correct home screen and clear the back stack
                    navController.navigate(homeScreen) {
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
                onRegisterSuccess = { user ->
                    // Navigate back to login or auto-login
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        // Event List Screen (Participant)
        composable(Screen.EventList.route) {
            EventListScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetails.createRoute(eventId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Event Detail Screen (Participant)
        composable(
            route = Screen.EventDetails.route,
            arguments = listOf(
                navArgument("eventId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailsScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = {
                    navController.navigate(Screen.CreateReservation.createRoute(eventId))
                }
            )
        }

        // Organizer Event Management Screen
        composable(Screen.OrganizerEventManagement.route) {
            OrganizerEventManagementScreen(
                onAddEventClick = {
                    navController.navigate(Screen.CreateEvent.route)
                },
                onEditEventClick = { eventId ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                },
                onDeleteEventClick = { /* Handle delete */ },
                onHomeClick = { // Corrected navigation
                    navController.navigate(Screen.OrganizerEventManagement.route) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Create Event Screen (Organizer)
        composable(Screen.CreateEvent.route) {
            val viewModel: EventFormViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // Handle success navigation
            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.popBackStack()
                }
            }

            EventFormScreen(
                eventId = null,
                isEditMode = false,
                initialEventData = null,
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { formData ->
                    // TODO: Get organizerId and organizerName from current user
                    // For now, using placeholder values
                    viewModel.submitEvent(
                        formData = formData,
                        isEditMode = false
                    )
                },
                onImageSelected = { uri ->
                    viewModel.onImageSelected(uri)
                }
            )
        }

        // Edit Event Screen (Organizer)
        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(
                navArgument("eventId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val viewModel: EventFormViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // Load event data when entering edit mode
            LaunchedEffect(eventId) {
                if (eventId.isNotEmpty()) {
                    viewModel.loadEvent(eventId)
                }
            }

            // Handle success navigation
            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.popBackStack()
                }
            }

            // Get initial form data from ViewModel (only when event is loaded)
            val initialFormData = if (uiState.event != null && !uiState.isLoading) {
                viewModel.getEventFormData()
            } else {
                null
            }

            EventFormScreen(
                eventId = eventId,
                isEditMode = true,
                initialEventData = initialFormData,
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { formData ->
                    viewModel.submitEvent(
                        formData = formData,
                        isEditMode = true
                    )
                },
                onImageSelected = { uri ->
                    viewModel.onImageSelected(uri)
                }
            )
        }

        // Admin Event Management Screen
        composable(Screen.AdminEventManagement.route) {
            AdminEventManagementScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.AdminEventApproval.createRoute(eventId))
                },
                onDeleteEventClick = { /* Handle delete */ },
                onHomeClick = { // Corrected navigation
                    navController.navigate(Screen.AdminEventManagement.route) {
                        launchSingleTop = true
                    }
                },
                onReservationClick = { /* TODO: Navigate to reservations */ },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Admin Event Approval Screen
        composable(
            route = Screen.AdminEventApproval.route,
            arguments = listOf(
                navArgument("eventId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            AdminEventApprovalScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onApproveClick = {
                    // TODO: Handle approval via ViewModel
                    navController.popBackStack()
                },
                onRejectClick = {
                    // TODO: Handle rejection via ViewModel
                    navController.popBackStack()
                },
                onCancelClick = { navController.popBackStack() }
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

            ReservationDetailScreen(
                reservationId = reservationId,
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

            UserReservationDetailScreen(
                reservationId = reservationId,
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
            val viewModel: com.example.sera_application.presentation.viewmodel.user.EditProfileViewModel = hiltViewModel()
            val user by viewModel.user.collectAsState()
            val updateSuccess by viewModel.updateSuccess.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val error by viewModel.error.collectAsState()
            val currentUser = user


            // Handle loading user if not present - use currentUser as dependency
            LaunchedEffect(currentUser) {
                if (currentUser == null) {
                    viewModel.loadCurrentUser()
                }
            }


            // Handle success navigation
            LaunchedEffect(updateSuccess) {
                if (updateSuccess) {
                    navController.popBackStack()
                }
            }


            when {
                currentUser != null -> {
                    EditUsernameScreen(
                        currentUsername = currentUser.fullName,
                        onBack = {
                            navController.popBackStack()
                        },
                        onConfirm = { newName ->
                            viewModel.updateUser(currentUser.copy(fullName = newName, updatedAt = System.currentTimeMillis()))
                        }
                    )
                }
                error != null -> {
                    // Show error state
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Text(
                            text = error ?: "Unknown error",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    // Show loading state by default if no user yet and no error
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }


// Change Password Screen
        composable(Screen.ChangePassword.route) {
            val viewModel: com.example.sera_application.presentation.viewmodel.user.EditProfileViewModel = hiltViewModel()
            val passwordUpdateSuccess by viewModel.passwordUpdateSuccess.collectAsState()


            LaunchedEffect(passwordUpdateSuccess) {
                if (passwordUpdateSuccess) {
                    navController.popBackStack()
                }
            }


            ChangePasswordScreen(
                onBack = {
                    navController.popBackStack()
                },
                onConfirm = { oldPassword, newPassword, confirmPassword ->
                    // confirmPassword check is already done in UI, but good to double check or just ignore
                    if (newPassword == confirmPassword) {
                        viewModel.updatePassword(
                            currentPassword = oldPassword,
                            newPassword = newPassword
                        )
                    }
                }
            )
        }


        // Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onEditUserName = {
                    navController.navigate(Screen.EditUsername.route)
                },
                onPasswordUpdate = {
                    navController.navigate(Screen.ChangePassword.route)
                },
                onOrderHistory = {
                    // Navigate to user's order history
                    navController.navigate(Screen.UserReservationHistory.route)
                },
                onPaymentHistory = {
                    navController.navigate(Screen.PaymentHistory.route)
                },
                onReservationManagement = {
                    navController.navigate(Screen.OrganizerPaymentManagement.route)
                },
                onPaymentHistoryOrganizer = {
                    navController.navigate(Screen.OrganizerPaymentManagement.route)
                },
                onReport = { },
                onUserManagement = {
                    navController.navigate(Screen.AdminUserManagement.route)
                },
                onEventApproval = { },
                onAdminReports = { },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onDeleteAccount = { },
                onHomeClick = {
                    navController.navigate(Screen.EventList.route) {
                        popUpTo(Screen.EventList.route) { inclusive = true }
                    }
                },
                onAddEventClick = { },
                onProfileClick = { }
            )
        }

        // Create Reservation Screen
        composable(
            route = Screen.CreateReservation.route,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            CreateReservationScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() },
                onReservationConfirmed = { reservationId ->
                    println("DEBUG: Navigating from Reservation to Payment: $reservationId")
                    navController.navigate(Screen.Payment.createRoute(reservationId))
                }
            )
        }

//        // Event List Screen
//        composable(Screen.EventList.route) {
//            EventListScreen(
//                onBack = {
//                    navController.popBackStack()
//                }
//            )
//        }

        // User List Screen
        composable(Screen.UserList.route) {
            UserListScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Reservation List Screen
        composable(
            route = Screen.ReservationList.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ReservationListScreen(
                userId = userId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin User Management Screen
        composable(Screen.AdminUserManagement.route) {
            com.example.sera_application.presentation.ui.user.AdminUserManagementScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Payment History Screen (User)
        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(
                onBack = { navController.popBackStack() },
                onViewReceipt = { orderData ->
                    navController.navigate(Screen.Receipt.createRoute(orderData.orderId))
                }
            )
        }

        // Organizer Payment Management Screen
        composable(Screen.OrganizerPaymentManagement.route) {
            OrganizerPaymentManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Payment Screen
        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument("reservationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
            
            LaunchedEffect(reservationId) {
                val intent = Intent(context, PaymentActivity::class.java).apply {
                    putExtra("RESERVATION_ID", reservationId)
                }
                context.startActivity(intent)
            }
            
            // Show a loading indicator while the activity is being launched
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Payment Status Screen
        composable(
            route = Screen.PaymentStatus.route,
            arguments = listOf(
                navArgument("paymentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            PaymentStatusScreen(
                paymentId = paymentId,
                onViewReceipt = { pid ->
                    navController.navigate(Screen.Receipt.createRoute(pid))
                },
                onBackToHome = {
                    navController.navigate(Screen.EventList.route) {
                        popUpTo(Screen.EventList.route) { inclusive = true }
                    }
                }
            )
        }

        // Receipt Screen
        composable(
            route = Screen.Receipt.route,
            arguments = listOf(
                navArgument("paymentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            ReceiptScreen(
                onBack = { navController.popBackStack() },
                onDownloadReceipt = { /* Handle download if needed or let screen handle */ },
                onRequestRefund = {
                    navController.navigate(Screen.RefundRequest.createRoute(paymentId))
                }
            )
        }

        // Refund Request Screen
        composable(
            route = Screen.RefundRequest.route,
            arguments = listOf(
                navArgument("paymentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Note: RefundRequestScreen might need paymentId via ViewModel or argument
            RefundRequestScreen(
                onBack = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}