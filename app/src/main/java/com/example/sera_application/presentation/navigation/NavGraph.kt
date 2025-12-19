package com.example.sera_application.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sera_application.presentation.ui.event.*
import com.example.sera_application.presentation.viewmodel.event.EventFormViewModel

/**
 * Main navigation graph for the application
 * Includes event management routes
 */
@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.EventList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
                onHomeClick = {
                    navController.navigate(Screen.OrganizerHome.route)
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
                        isEditMode = false,
                        organizerId = "organizer_1", // TODO: Get from current user
                        organizerName = "Organizer Name" // TODO: Get from current user
                    )
                },
                onImagePickClick = { /* TODO: Handle image pick */ }
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
                onImagePickClick = { /* TODO: Handle image pick */ }
            )
        }

        // Admin Event Management Screen
        composable(Screen.AdminEventManagement.route) {
            AdminEventManagementScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.AdminEventApproval.createRoute(eventId))
                },
                onDeleteEventClick = { /* Handle delete */ },
                onHomeClick = {
                    navController.navigate(Screen.AdminDashboard.route)
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
    }
}
