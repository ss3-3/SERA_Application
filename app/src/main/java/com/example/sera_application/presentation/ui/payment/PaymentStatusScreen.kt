package com.example.sera_application.presentation.ui.payment


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.sera_application.ui.theme.SERA_ApplicationTheme
import com.example.sera_application.MainActivity
import java.util.Locale
import com.example.sera_application.utils.bottomNavigationBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.domain.model.enums.UserRole
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint


@Composable
fun PaymentStatusScreen(
    paymentId: String,
    onViewReceipt: (String) -> Unit,
    onBackToHome: () -> Unit,
    onProfileClick: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    // For now, redirecting to Success screen with the provided paymentId
    // In a real app, you might fetch status from a ViewModel
    PaymentSuccessScreen(
        transactionId = paymentId,
        amount = 70.0, // Default for mock
        onHomeClick = onBackToHome,
        onProfileClick = onProfileClick,
        navController = navController
    )
}


@AndroidEntryPoint
class PaymentStatusActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val isSuccess = intent.getBooleanExtra("PAYMENT_SUCCESS", true)
        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: "1234-1234-1234"
        val amount = intent.getDoubleExtra("AMOUNT", 70.0)
        val reservationId = intent.getStringExtra("RESERVATION_ID")
        val failureReason = intent.getStringExtra("FAILURE_REASON") ?: "Insufficient balance in your PayPal account."


        setContent {
            SERA_ApplicationTheme {
                if (isSuccess) {
                    PaymentSuccessScreen(
                        transactionId = transactionId,
                        amount = amount,
                        reservationId = reservationId,
                        onHomeClick = {
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(intent)
                        },
                        onProfileClick = {
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("DESTINATION", "profile")
                            }
                            startActivity(intent)
                        }
                    )
                } else {
                    PaymentFailScreen(
                        failureReason = failureReason,
                        orderDetails = "Order #1234 • 2 tickets • RM ${String.format(Locale.US, "%.2f", amount)}",
                        onHomeClick = {
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(intent)
                        },
                        onProfileClick = {
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("DESTINATION", "profile")
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun PaymentSuccessScreen(
    transactionId: String,
    amount: Double,
    reservationId: String? = null,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    val context = LocalContext.current
    val currentDate = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date())
    val currentTime = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date())

    // Get current user for role-based navigation
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val currentUser by profileViewModel.user.collectAsState()
    
    // Load current user
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }

    Scaffold(
        bottomBar = {
            navController?.let { nav ->
                bottomNavigationBar(
                    navController = nav,
                    currentRoute = nav.currentBackStackEntry?.destination?.route,
                    userRole = currentUser?.role ?: UserRole.PARTICIPANT
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))


            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color(0xFF4CAF50)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(24.dp))


            Text(
                text = "Payment",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Successful!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )


            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = "Your payment has been processed successfully.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )


            Spacer(modifier = Modifier.height(32.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    TransactionDetailRow("Transaction ID", transactionId)
                    Spacer(modifier = Modifier.height(16.dp))


                    TransactionDetailRow("Amount Paid", "RM${String.format(Locale.US, "%.2f", amount)}")
                    Spacer(modifier = Modifier.height(16.dp))


                    TransactionDetailRow("Payment Method", "PayPal")
                    Spacer(modifier = Modifier.height(16.dp))


                    TransactionDetailRow("Date", currentDate)
                    Spacer(modifier = Modifier.height(16.dp))


                    TransactionDetailRow("Time", currentTime)
                }
            }


            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    val intent = Intent(context, ReceiptActivity::class.java).apply {
                        putExtra("TRANSACTION_ID", transactionId)
                        putExtra("RESERVATION_ID", reservationId)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B9FED)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "View Receipt",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun PaymentFailScreen(
    failureReason: String,
    orderDetails: String,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    val context = LocalContext.current

    // Get current user for role-based navigation
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val currentUser by profileViewModel.user.collectAsState()
    
    // Load current user
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }

    Scaffold(
        bottomBar = {
            navController?.let { nav ->
                bottomNavigationBar(
                    navController = nav,
                    currentRoute = nav.currentBackStackEntry?.destination?.route,
                    userRole = currentUser?.role ?: UserRole.PARTICIPANT
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))


            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color(0xFFFFA726)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }


            Spacer(modifier = Modifier.height(24.dp))


            Text(
                text = "Payment",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Failed!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )


            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = "Your payment could not be",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "processed.",
                fontSize = 16.sp,
                color = Color.Gray
            )


            Spacer(modifier = Modifier.height(32.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Reason:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = failureReason,
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )


                    Spacer(modifier = Modifier.height(16.dp))


                    Text(
                        text = "Order Details:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = orderDetails,
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    (context as? ComponentActivity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B9FED)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Retry Payment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }


            Spacer(modifier = Modifier.height(12.dp))


            Button(
                onClick = {
                    Toast.makeText(context, "Modify Order", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B9FED)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Modify Order",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun TransactionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
