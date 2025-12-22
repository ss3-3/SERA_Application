package com.example.sera_application.presentation.ui.payment


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.example.sera_application.utils.BottomNavigationBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.presentation.viewmodel.reservation.ReservationDetailsViewModel
import com.example.sera_application.domain.model.enums.UserRole
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.payment.GetPaymentByIdUseCase
import com.example.sera_application.domain.usecase.reservation.GetReservationByIdUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.sera_application.utils.PdfReceiptGenerator
import com.example.sera_application.utils.ReceiptData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun PaymentStatusScreen(
    paymentId: String,
    onViewReceipt: (String) -> Unit,
    onBackToHome: () -> Unit,
    onProfileClick: () -> Unit,
    navController: NavController? = null
) {
    // Check if this is a free reservation by fetching the reservation
    val reservationViewModel: ReservationDetailsViewModel = hiltViewModel()
    val reservationState by reservationViewModel.uiState.collectAsState()
    
    // Load reservation to check if it's free
    LaunchedEffect(paymentId) {
        reservationViewModel.loadReservation(paymentId)
    }
    
    val reservation = reservationState.reservation
    val isFreeReservation = reservation?.totalPrice == 0.0
    
    PaymentSuccessScreen(
        transactionId = paymentId,
        amount = reservation?.totalPrice ?: 0.0,
        reservationId = if (isFreeReservation) paymentId else null,
        isFreeReservation = isFreeReservation,
        onHomeClick = onBackToHome,
        onProfileClick = onProfileClick,
        navController = navController,
        onViewReceipt = {
            onViewReceipt(paymentId)
        }
    )
}


@AndroidEntryPoint
class PaymentStatusActivity : ComponentActivity() {
    
    @Inject
    lateinit var getPaymentByIdUseCase: GetPaymentByIdUseCase
    
    @Inject
    lateinit var getReservationByIdUseCase: GetReservationByIdUseCase
    
    @Inject
    lateinit var getEventByIdUseCase: GetEventByIdUseCase
    
    @Inject
    lateinit var getUserProfileUseCase: GetUserProfileUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if this activity was started to generate PDF only
        val generatePdf = intent.getBooleanExtra("GENERATE_PDF", false)
        if (generatePdf) {
            val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: ""
            val reservationId = intent.getStringExtra("RESERVATION_ID")
            
            if (transactionId.isNotEmpty()) {
                // Generate PDF and finish activity after completion
                generateAndOpenPdf(transactionId, reservationId) {
                    // Finish activity after PDF is opened (with a small delay)
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 500)
                }
                
                // Show a minimal UI while generating PDF
                setContent {
                    SERA_ApplicationTheme {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Generating receipt...")
                            }
                        }
                    }
                }
                return
            }
        }

        val isSuccess = intent.getBooleanExtra("PAYMENT_SUCCESS", true)
        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: "1234-1234-1234"
        val amount = intent.getDoubleExtra("AMOUNT", 70.0)
        val reservationId = intent.getStringExtra("RESERVATION_ID")
        val failureReason = intent.getStringExtra("FAILURE_REASON") ?: "Insufficient balance in your PayPal account."
        val ticketCount = intent.getIntExtra("TICKET_COUNT", 1)
        val eventName = intent.getStringExtra("EVENT_NAME") ?: "Event"


        setContent {
            SERA_ApplicationTheme {
                if (isSuccess) {
                    PaymentSuccessScreen(
                        transactionId = transactionId,
                        amount = amount,
                        reservationId = reservationId,
                        isFreeReservation = amount == 0.0, // Check if free based on amount
                        onViewReceipt = {
                            // Navigate to ReceiptActivity (receipt screen)
                            val intent = Intent(this@PaymentStatusActivity, com.example.sera_application.presentation.ui.payment.ReceiptActivity::class.java).apply {
                                putExtra("TRANSACTION_ID", transactionId)
                            }
                            startActivity(intent)
                        },
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
                        },
                        navController = null // Activity doesn't have navController
                    )
                } else {
                    PaymentFailScreen(
                        failureReason = failureReason,
                        orderDetails = "${if (reservationId != null) "Order #${reservationId.take(8)}" else "Order"} • $ticketCount ticket${if (ticketCount > 1) "s" else ""} • RM ${String.format(Locale.US, "%.2f", amount)}",
                        eventName = eventName,
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

    private fun generateAndOpenPdf(transactionId: String, reservationId: String?, onComplete: (() -> Unit)? = null) {
        lifecycleScope.launch {
            try {
                Log.d("PaymentStatus", "Starting PDF generation for transaction: $transactionId")
                Toast.makeText(
                    this@PaymentStatusActivity,
                    "Generating receipt...",
                    Toast.LENGTH_SHORT
                ).show()

                val pdfFile: File = withContext(Dispatchers.IO) {
                    try {
                        Log.d("PaymentStatus", "Loading payment data for: $transactionId")
                        // Load payment data
                        val payment = getPaymentByIdUseCase(transactionId)
                        Log.d("PaymentStatus", "Payment loaded: ${payment != null}")
                        
                        // Load reservation if available
                        val reservation = reservationId?.let { 
                            Log.d("PaymentStatus", "Loading reservation: $it")
                            getReservationByIdUseCase(it) 
                        }
                        Log.d("PaymentStatus", "Reservation loaded: ${reservation != null}")
                        
                        // Load event
                        val event = payment?.let { 
                            Log.d("PaymentStatus", "Loading event: ${it.eventId}")
                            getEventByIdUseCase(it.eventId) 
                        }
                        Log.d("PaymentStatus", "Event loaded: ${event != null}")
                        
                        // Load user
                        val user = payment?.let { 
                            Log.d("PaymentStatus", "Loading user: ${it.userId}")
                            getUserProfileUseCase(it.userId) 
                        }
                        Log.d("PaymentStatus", "User loaded: ${user != null}")
                        
                        // Format date and time
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val paymentDate = payment?.let { Date(it.createdAt) } ?: Date()
                        
                        // Calculate ticket quantity
                        val quantity = reservation?.let { 
                            (it.rockZoneSeats ?: 0) + (it.normalZoneSeats ?: 0)
                        } ?: 1
                        
                        Log.d("PaymentStatus", "Creating ReceiptData with quantity: $quantity")
                        val receiptData = ReceiptData(
                            eventName = event?.name ?: "Event",
                            transactionId = transactionId,
                            date = dateFormat.format(paymentDate),
                            time = timeFormat.format(paymentDate),
                            venue = event?.location ?: "Unknown",
                            ticketType = "NORMAL",
                            quantity = quantity,
                            seats = "N/A", // Could be enhanced to show actual seats
                            price = payment?.amount ?: 0.0,
                            email = user?.email ?: "",
                            name = user?.fullName ?: "",
                            phone = user?.phone ?: ""
                        )

                        Log.d("PaymentStatus", "Generating PDF...")
                        val generator = PdfReceiptGenerator(this@PaymentStatusActivity)
                        val file = generator.generateReceipt(receiptData)
                        Log.d("PaymentStatus", "PDF generated successfully: ${file.absolutePath}")
                        file
                    } catch (e: Exception) {
                        Log.e("PaymentStatus", "Error in PDF generation: ${e.message}", e)
                        e.printStackTrace()
                        throw e
                    }
                }

                Log.d("PaymentStatus", "Creating FileProvider URI...")
                val uri = FileProvider.getUriForFile(
                    this@PaymentStatusActivity,
                    "${applicationContext.packageName}.fileprovider",
                    pdfFile
                )

                Log.d("PaymentStatus", "Opening PDF viewer...")
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }

                startActivity(intent)
                Log.d("PaymentStatus", "PDF opened successfully")
                
                // Call completion callback if provided
                onComplete?.invoke()

            } catch (e: Exception) {
                Log.e("PaymentStatus", "Error generating receipt: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(
                    this@PaymentStatusActivity,
                    "Error generating receipt: ${e.message}\n${e.javaClass.simpleName}",
                    Toast.LENGTH_LONG
                ).show()
                
                // Still call completion callback even on error
                onComplete?.invoke()
            }
        }
    }
}


@Composable
fun PaymentSuccessScreen(
    transactionId: String,
    amount: Double,
    reservationId: String? = null,
    isFreeReservation: Boolean = false,
    onViewReceipt: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val currentDate = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())
    val currentTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())

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
                BottomNavigationBar(
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
                text = if (isFreeReservation) "Reservation" else "Payment",
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
                text = if (isFreeReservation) 
                    "Your reservation has been confirmed successfully." 
                else 
                    "Your payment has been processed successfully.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
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
                    TransactionDetailRow(
                        if (isFreeReservation) "Reservation ID" else "Transaction ID", 
                        transactionId
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isFreeReservation) {
                        TransactionDetailRow("Amount Paid", "RM${String.format(Locale.US, "%.2f", amount)}")
                        Spacer(modifier = Modifier.height(16.dp))

                        TransactionDetailRow("Payment Method", "PayPal")
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        TransactionDetailRow("Amount", "Free")
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    TransactionDetailRow("Date", currentDate)
                    Spacer(modifier = Modifier.height(16.dp))

                    TransactionDetailRow("Time", currentTime)
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            // Only show receipt button for paid reservations
            if (!isFreeReservation) {
                Button(
                    onClick = {
                        // Always generate and open PDF, never navigate to receipt screen
                        onViewReceipt()
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

            // Show "View Detail" button only for free reservations
            if (isFreeReservation && reservationId != null && navController != null) {
                Button(
                    onClick = {
                        navController.navigate("user_reservation_detail/$reservationId")
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
                        text = "View Detail",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun PaymentFailScreen(
    failureReason: String,
    orderDetails: String,
    eventName: String = "Event",
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    navController: NavController? = null
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
                BottomNavigationBar(
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
                        text = "Event:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = eventName,
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
