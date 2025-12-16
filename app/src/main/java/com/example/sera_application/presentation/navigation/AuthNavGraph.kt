package com.example.sera_application.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sera_application.presentation.ui.auth.LoginScreen
import com.example.sera_application.presentation.ui.auth.SignUpScreen

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password, rememberMe ->
                    // Handle login logic here
                    // For now, just navigate on success
                    onLoginSuccess()
                },
                onForgotPasswordClick = {
                    onForgotPassword()
                },
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onRegisterClick = { name, email, password, confirmPassword ->
                    // Handle registration logic here
                    // For now, just navigate to login on success
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}