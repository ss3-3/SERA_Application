package com.example.sera_application.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.sera_application.presentation.viewmodel.UserViewModel
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
                    // TODO: Navigate to reservation/payment
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
            val viewModel: UserViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val currentUser = uiState.currentUser

            // Handle loading user if not present (though Profile should have loaded it)
            LaunchedEffect(Unit) {
                if (currentUser == null) {
                    viewModel.loadCurrentUser()
                }
            }

            if (currentUser != null) {
                EditUsernameScreen(
                    currentUsername = currentUser.fullName,
                    onBack = {
                        navController.popBackStack()
                    },
                    onConfirm = { newName ->
                        viewModel.updateUsername(
                            userId = currentUser.userId,
                            newName = newName,
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onError = { error ->
                                // Optional: Show error (passed via nav args or handled in VM state)
                                // identifying error handling strategy later
                            }
                        )
                    }
                )
            } else {
                 // Show loading or fallback
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     CircularProgressIndicator()
                 }
            }
        }

        // Change Password Screen
        composable(Screen.ChangePassword.route) {
            val viewModel: UserViewModel = hiltViewModel()

            ChangePasswordScreen(
                onBack = {
                    navController.popBackStack()
                },
                onConfirm = { oldPassword, newPassword, confirmPassword ->
                     // confirmPassword check is already done in UI, but good to double check or just ignore
                    if (newPassword == confirmPassword) {
                        viewModel.updatePassword(
                            currentPassword = oldPassword,
                            newPassword = newPassword,
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onError = { error ->
                                // Optional: Show error
                            }
                        )
                    }
                }
            )
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            val userState by userViewModel.uiState.collectAsState()
            val currentUser = userState.currentUser

            // Load user to determine their role for navigation
            LaunchedEffect(Unit) {
                if (currentUser == null) {
                    userViewModel.loadCurrentUser()
                }
            }

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
                onPaymentHistory = { },
                onReservationManagement = {
                    navController.navigate(Screen.ReservationManagement.route)
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
                    val homeScreen = when (currentUser?.role) {
                        UserRole.ORGANIZER -> Screen.OrganizerEventManagement.route
                        UserRole.ADMIN -> Screen.AdminEventManagement.route
                        else -> Screen.EventList.route
                    }
                    navController.navigate(homeScreen) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
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
                onBack = {
                    navController.popBackStack()
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
    }
}
