package com.example.sera_application.presentation.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import com.example.sera_application.presentation.ui.user.UserListScreen
import com.example.sera_application.presentation.ui.reservation.ReservationListScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.sera_application.presentation.ui.auth.OrganizerWaitingApprovalScreen
import com.example.sera_application.presentation.ui.notification.NotificationListScreen
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
import com.example.sera_application.presentation.ui.payment.PaymentActivity
import com.example.sera_application.presentation.ui.payment.*
import com.example.sera_application.presentation.viewmodel.event.EventFormViewModel
import com.example.sera_application.presentation.navigation.asNavigationController

fun NavHostController.navigateToReservationDetails(reservationId: String) {
    navigate(Screen.ReservationDetails.createRoute(reservationId))
}

@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val navigationController = remember(navController) {
        navController.asNavigationController()
    }
    
    CompositionLocalProvider(LocalNavigationController provides navigationController) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    // Check if organizer is approved before navigating
                    if (user.role == UserRole.ORGANIZER && !user.isApproved) {
                        // Navigate to waiting approval screen
                        navController.navigate(Screen.OrganizerWaitingApproval.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
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
                    navigationController.navigateToLogin()
                },
                onLoginClick = {
                    navigationController.navigateToLogin()
                }
            )
        }

        // Organizer Waiting Approval Screen
        composable(Screen.OrganizerWaitingApproval.route) {
            OrganizerWaitingApprovalScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Event List Screen (Participant)
        composable(Screen.EventList.route) {
            val profileViewModel: com.example.sera_application.presentation.viewmodel.user.ProfileViewModel = hiltViewModel()
            val currentUser by profileViewModel.user.collectAsState()
            
            // Load current user on first composition
            LaunchedEffect(Unit) {
                profileViewModel.loadCurrentUser()
            }
            
            EventListScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetails.createRoute(eventId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onNotificationClick = {
                    currentUser?.userId?.let { userId ->
                        navController.navigate(Screen.Notifications.createRoute(userId))
                    }
                },
                navController = navController
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
                },
                navController = navController
            )
        }

        // Create Event Screen (Organizer)
        composable(Screen.CreateEvent.route) {
            val viewModel: EventFormViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // Handle success navigation
            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navigationController.navigateBack()
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
                },
                currentImagePath = uiState.imagePath
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
                    navigationController.navigateBack()
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
                },
                currentImagePath = uiState.imagePath
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
                    navigationController.navigateBack()
                },
                onRejectClick = {
                    // TODO: Handle rejection via ViewModel
                    navigationController.navigateBack()
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
                    navigationController.navigateBack()
                }
            )
        }

        // Reservation Management Screen (Organizer)
        composable(Screen.ReservationManagement.route) {
            ReservationManagementScreen(
                onBack = {
                    navigationController.navigateBack()
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
                    navigationController.navigateBack()
                },
                onViewDetails = { reservation ->
                    navController.navigate("user_reservation_detail/${reservation.reservationId}")
                },
                onCancelReservation = { reservation ->
                    android.util.Log.d("NavGraph", "onCancelReservation clicked for: ${reservation.reservationId}, paymentId: ${reservation.paymentId}")
                    reservation.paymentId?.let { paymentId ->
                        if (paymentId.isNotEmpty()) {
                            navController.navigate(Screen.RefundRequest.createRoute(paymentId))
                        } else {
                            android.widget.Toast.makeText(navController.context, "Payment ID is empty", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        android.widget.Toast.makeText(navController.context, "No payment information for this reservation", android.widget.Toast.LENGTH_SHORT).show()
                    }
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
                    navigationController.navigateBack()
                },
                onCancelReservation = {
                    // TODO: Handle cancel reservation from detail screen
                    navigationController.navigateBack()
                }
            )
        }


        // Notification Screen
        composable(
            route = Screen.Notifications.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            NotificationListScreen(
                userId = userId,
                onBackClick = {
                    navigationController.navigateBack()
                },
                onNavigateToEvent = { eventId ->
                    navController.navigate(Screen.EventDetails.createRoute(eventId))
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
                    navigationController.navigateBack()
                }
            }


            when {
                currentUser != null -> {
                    EditUsernameScreen(
                        currentUsername = currentUser.fullName,
                        onBack = {
                            navigationController.navigateBack()
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
            val viewModel: com.example.sera_application.presentation.viewmodel.user.EditProfileViewModel =
                hiltViewModel()
            val passwordUpdateSuccess by viewModel.passwordUpdateSuccess.collectAsState()


            LaunchedEffect(passwordUpdateSuccess) {
                if (passwordUpdateSuccess) {
                    navigationController.navigateBack()
                }
            }


            ChangePasswordScreen(navController = navController)

        }
        // Profile Screen
        composable(Screen.Profile.route) {
            val profileViewModel: com.example.sera_application.presentation.viewmodel.user.ProfileViewModel = hiltViewModel()
            val currentUser by profileViewModel.user.collectAsState()
            
            // Load current user on first composition
            LaunchedEffect(Unit) {
                profileViewModel.loadCurrentUser()
            }
            
            ProfileScreen(
                onBack = {
                    navigationController.navigateBack()
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
                onDeleteAccount = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController,
                onHomeClick = {
                    // Navigate to home based on user role
                    val homeScreen = when (currentUser?.role) {
                        UserRole.ORGANIZER -> Screen.OrganizerEventManagement.route
                        UserRole.ADMIN -> Screen.AdminEventManagement.route
                        else -> Screen.EventList.route // PARTICIPANT
                    }
                    navController.navigate(homeScreen) {
                        popUpTo(homeScreen) { inclusive = true }
                    }
                },
                onAddEventClick = {
                    // Navigate based on user role
                    when (currentUser?.role) {
                        UserRole.ORGANIZER -> {
                            navController.navigate(Screen.CreateEvent.route)
                        }
                        UserRole.ADMIN -> {
                            navController.navigate(Screen.AdminEventManagement.route) {
                                launchSingleTop = true
                            }
                        }
                        else -> {
                            // PARTICIPANT - navigate to event list
                            navController.navigate(Screen.EventList.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                },
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
                onBack = {
                    // Navigate back to Event Details screen (previous screen in stack)
                    navController.popBackStack()
                },
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
//                    navigationController.navigateBack()
//                }
//            )
//        }

        // User List Screen
        composable(Screen.UserList.route) {
            UserListScreen(navController)
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
                    navigationController.navigateBack()
                }
            )
        }

        // Admin User Management Screen
        composable(Screen.AdminUserManagement.route) {
            com.example.sera_application.presentation.ui.user.AdminUserManagementScreen(navController)
        }

        // Payment History Screen (User)
        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(
                onViewReceipt = { orderData ->
                    // OrderData has orderId property
                    navController.navigate(Screen.Receipt.createRoute(orderData.orderId))
                },
                navController = navController
            )
        }

        // Organizer Payment Management Screen
        composable(Screen.OrganizerPaymentManagement.route) {
            OrganizerPaymentManagementScreen(
                onBack = { navController.popBackStack() },
                navController = navController
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
                // Pop this launcher route so back button from PaymentActivity goes to CreateReservation
                navController.popBackStack()
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
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                navController = navController
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
            // ReceiptScreen requires ViewModel - redirect to ReceiptActivity instead
            val context = LocalContext.current
            LaunchedEffect(paymentId) {
                val intent = android.content.Intent(context, com.example.sera_application.presentation.ui.payment.ReceiptActivity::class.java).apply {
                    putExtra("TRANSACTION_ID", paymentId)
                }
                context.startActivity(intent)
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Refund Request Screen
        composable(
            route = Screen.RefundRequest.route,
            arguments = listOf(
                navArgument("paymentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            val context = LocalContext.current
            LaunchedEffect(paymentId) {
                if (paymentId.isNotEmpty()) {
                    val intent = android.content.Intent(context, com.example.sera_application.presentation.ui.payment.RefundRequestActivity::class.java).apply {
                        putExtra("PAYMENT_ID", paymentId)
                    }
                    context.startActivity(intent)
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        }
    }
}