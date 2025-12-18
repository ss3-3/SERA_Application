package com.example.sera_application.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sera_application.presentation.ui.event.*
import com.example.sera_application.presentation.viewmodel.event.EventFormViewModel

// Screen.kt - Define routes
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
}

// EventNavGraph.kt
@Composable
fun EventNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.EventList.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        // Participant screens
        composable(Screen.EventList.route) {
            EventListScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetails.createRoute(eventId))
                }
            )
        }

        composable(
            route = Screen.EventDetails.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailsScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Organizer screens
        composable(Screen.OrganizerEventManagement.route) {
            OrganizerEventManagementScreen(
                organizerId = "organizer_1", // TODO: Get from auth
                onAddEventClick = {
                    navController.navigate(Screen.CreateEvent.route)
                },
                onEditEventClick = { eventId ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                }
            )
        }

        composable(Screen.CreateEvent.route) {
            CreateEventScreen(
                onBackClick = { navController.popBackStack() },
                onCreateClick = { formData ->
                    // Handle success, navigate back
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EditEventScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { formData ->
                    navController.popBackStack()
                }
            )
        }

        // Admin screens
        composable(Screen.AdminEventManagement.route) {
            AdminEventManagementScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.AdminEventApproval.createRoute(eventId))
                }
            )
        }

        composable(
            route = Screen.AdminEventApproval.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            AdminEventApprovalScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onApproveClick = { navController.popBackStack() },
                onRejectClick = { navController.popBackStack() },
                onCancelClick = { navController.popBackStack() }
            )
        }
    }
}
