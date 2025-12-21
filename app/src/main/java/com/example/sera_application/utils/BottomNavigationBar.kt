package com.example.sera_application.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.presentation.navigation.Screen

/**
 * Shared bottom navigation bar that handles role-based navigation for all screens.
 * 
 * @param navController NavController for navigation
 * @param currentRoute Current navigation route to highlight selected item
 * @param userRole User's role (PARTICIPANT, ORGANIZER, or ADMIN)
 * 
 * Navigation structure:
 * - PARTICIPANT: Home (EventList) | Me (Profile)
 * - ORGANIZER: Home (OrganizerEventManagement) | Add Event (+) | Me (Profile)
 * - ADMIN: Home (AdminDashboard) | Event | Reservation | Me (Profile)
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?,
    userRole: UserRole?
) {
    when (userRole) {
        UserRole.PARTICIPANT -> {
            ParticipantBottomNav(navController, currentRoute)
        }
        UserRole.ORGANIZER -> {
            OrganizerBottomNav(navController, currentRoute)
        }
        UserRole.ADMIN -> {
            AdminBottomNav(navController, currentRoute)
        }
        else -> {
            // Default to participant navigation if role is unknown
            ParticipantBottomNav(navController, currentRoute)
        }
    }
}

@Composable
private fun ParticipantBottomNav(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 12.sp) },
            selected = isHomeRoute(currentRoute, UserRole.PARTICIPANT),
            onClick = {
                navController.navigate(Screen.EventList.route) {
                    popUpTo(Screen.EventList.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Me", fontSize = 12.sp) },
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun OrganizerBottomNav(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 12.sp) },
            selected = isHomeRoute(currentRoute, UserRole.ORGANIZER),
            onClick = {
                navController.navigate(Screen.OrganizerEventManagement.route) {
                    popUpTo(Screen.OrganizerEventManagement.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        
        // Center Add Event button with custom styling
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color.DarkGray,
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Event",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            label = { }, // Empty label for center button
            selected = false,
            onClick = {
                navController.navigate(Screen.CreateEvent.route) {
                    launchSingleTop = true
                }
            }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Me", fontSize = 12.sp) },
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun AdminBottomNav(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Event, contentDescription = "Event") },
            label = { Text("Event") },
            selected = currentRoute == Screen.AdminEventManagement.route,
            onClick = {
                navController.navigate(Screen.AdminEventManagement.route) {
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BookOnline, contentDescription = "Reservation") },
            label = { Text("Reservation") },
            selected = currentRoute == Screen.AdminReservationManagement.route,
            onClick = {
                navController.navigate(Screen.AdminReservationManagement.route) {
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Me") },
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    launchSingleTop = true
                }
            }
        )
    }
}

/**
 * Helper function to determine if current route is a home route for the given role
 */
private fun isHomeRoute(
    currentRoute: String?,
    userRole: UserRole
): Boolean {
    return when (userRole) {
        UserRole.PARTICIPANT ->
            currentRoute == Screen.EventList.route

        UserRole.ORGANIZER ->
            currentRoute == Screen.OrganizerEventManagement.route

        UserRole.ADMIN ->
            currentRoute == Screen.AdminEventManagement.route

        else -> false
    }
}