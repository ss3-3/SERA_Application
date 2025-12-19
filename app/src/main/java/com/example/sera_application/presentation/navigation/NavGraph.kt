package com.example.sera_application.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
//import com.example.sera_application.presentation.ui.event.EventListScreen
import com.example.sera_application.presentation.ui.user.UserListScreen
import com.example.sera_application.presentation.ui.reservation.ReservationListScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
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
//import com.example.sera_application.presentation.viewmodel.UserViewModel
import com.example.sera_application.presentation.ui.user.ProfileScreen
import com.google.firebase.auth.FirebaseAuth

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
                onLoginSuccess = { user ->
                    // Set user role based on authenticated user from Firebase
                    selectedUserRole = user.role

                    // Navigate to Profile
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
                onRegisterSuccess = { user ->
                    // Navigate back to login or auto-login
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
            val viewModel: com.example.sera_application.presentation.viewmodel.UserViewModel = hiltViewModel()
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
            val viewModel: com.example.sera_application.presentation.viewmodel.UserViewModel = hiltViewModel()
            
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
