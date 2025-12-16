//package com.example.sera_application.presentation.ui.reservation
//
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import com.example.sera_application.domain.model.enums.UserRole
//import com.example.sera_application.presentation.navigation.Screen
//
//var selectedUserRole = mutableStateOf(UserRole.PARTICIPANT)
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomePageScreen(
//    navController: NavHostController? = null,
//    modifier: Modifier = Modifier,
//    onUserClick: () -> Unit = {},
//    onOrganizerClick: () -> Unit = {},
//    onAdminClick: () -> Unit = {},
//    onCreateReservationClick: () -> Unit = {},
//    onLoginClick: () -> Unit = {},
//    onSignUpClick: () -> Unit = {}
//) {
//    Scaffold(
//        modifier = modifier.fillMaxSize(),
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        "Reservation Module",
//                        color = Color.White,
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color(0xFF1976D2) // Blue header
//                )
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "Select User Type",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black,
//                modifier = Modifier.padding(bottom = 48.dp)
//            )
//
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//
//            // User Button - Navigate to ProfileScreen as PARTICIPANT
//            Button(
//                onClick = {
//                    selectedUserRole.value = UserRole.PARTICIPANT
//                    navController?.navigate(Screen.Profile.route) ?: onUserClick()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(64.dp),
//                shape = MaterialTheme.shapes.medium,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF1976D2),
//                    contentColor = Color.White
//                )
//            ) {
//                Text(
//                    text = "User",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//
//            // Organizer Button - Navigate to ProfileScreen as ORGANIZER
//            Button(
//                onClick = {
//                    selectedUserRole.value = UserRole.ORGANIZER
//                    navController?.navigate(Screen.Profile.route) ?: onOrganizerClick()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(64.dp),
//                shape = MaterialTheme.shapes.medium,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF4CAF50),
//                    contentColor = Color.White
//                )
//            ) {
//                Text(
//                    text = "Organizer",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//
//            // Admin Button - Navigate to ProfileScreen as ADMIN
//            Button(
//                onClick = {
//                    selectedUserRole.value = UserRole.ADMIN
//                    navController?.navigate(Screen.Profile.route) ?: onAdminClick()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(64.dp),
//                shape = MaterialTheme.shapes.medium,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFF44336),
//                    contentColor = Color.White
//                )
//            ) {
//                Text(
//                    text = "Admin",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//
//
//            Spacer(modifier = Modifier.height(22.dp))
//
//            // Reservation Button
//            Button(
//                onClick = {
//                    navController?.navigate(Screen.CreateReservation.route) ?: onCreateReservationClick()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = MaterialTheme.shapes.medium,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF9C27B0), // Purple color
//                    contentColor = Color.White
//                )
//            ) {
//                Text(
//                    text = "Reservation",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//
//            // Login Button
//            Button(
//                onClick = {
//                    navController?.navigate(Screen.Login.route) ?: onLoginClick()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = MaterialTheme.shapes.medium,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF2196F3),
//                    contentColor = Color.White
//                )
//            ) {
//                Text(
//                    text = "Login",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//
//            // SignUp Button
//            OutlinedButton(
//                onClick = {
//                    navController?.navigate(Screen.SignUp.route) ?: onSignUpClick()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = MaterialTheme.shapes.medium,
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = Color(0xFF1F7AE0)
//                )
//            ) {
//                Text(
//                    text = "Sign Up",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//        }
//    }
//}
//
//
//@Preview(showBackground = true)
//@Composable
//private fun HomePageScreenPreview() {
//    MaterialTheme {
//        HomePageScreen()
//    }
//}
