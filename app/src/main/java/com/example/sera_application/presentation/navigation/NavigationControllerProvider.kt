package com.example.sera_application.presentation.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

/**
 * CompositionLocal provider for NavigationController.
 * This allows screens to access NavigationController without passing it explicitly.
 */
val LocalNavigationController = compositionLocalOf<NavigationController> {
    error("No NavigationController provided")
}

/**
 * Extension function to create NavigationController from NavController.
 */
fun NavController.asNavigationController(): NavigationController {
    return NavigationControllerImpl(this)
}

