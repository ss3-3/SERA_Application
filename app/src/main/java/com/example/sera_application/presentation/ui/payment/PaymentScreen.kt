package com.example.sera_application.presentation.ui.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.sera_application.BottomNavigationBar
import com.example.sera_application.R
import com.example.sera_application.ui.theme.SERA_ApplicationTheme
import com.example.sera_application.data.remote.paypal.repository.PayPalRepository
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.data.remote.datasource.PaymentRemoteDataSource
import com.example.sera_application.presentation.viewmodel.payment.PaymentScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaymentActivity : ComponentActivity() {

    @Inject
    lateinit var paymentRemoteDataSource: PaymentRemoteDataSource
    
    @Inject
    lateinit var paypalRepository: PayPalRepository
    
    private var pendingOrderId: String? = null
    private val isProcessingPayment = mutableStateOf(false)
    private val viewModel: PaymentScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reservationId = intent.getStringExtra("RESERVATION_ID") ?: ""
        
        // Load reservation data
        if (reservationId.isNotEmpty()) {
            viewModel.loadReservationDetails(reservationId)
        }

        handleDeepLink(intent)

        setContent {
            SERA_ApplicationTheme {
                val reservation by viewModel.reservation.collectAsState()
                val event by viewModel.event.collectAsState()
                val isLoadingData by viewModel.isLoading.collectAsState()
                
                PaymentScreen(
                    reservationId = reservationId,
                    reservation = reservation,
                    event = event,
                    isLoadingData = isLoadingData,
                    onBack = { finish() },
                    onPaymentSuccess = { paymentId ->
                        val totalAmount = reservation?.totalPrice ?: 70.0
                        val intent = Intent(this, PaymentStatusActivity::class.java).apply {
                            putExtra("PAYMENT_SUCCESS", true)
                            putExtra("TRANSACTION_ID", paymentId)
                            putExtra("AMOUNT", totalAmount)
                        }
                        startActivity(intent)
                        finish()
                    },
                    isProcessing = isProcessingPayment.value,
                    onPayPalCheckout = { startPayPalCheckout() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "sera") {
            // Log the full deep link URL for debugging
            Log.d("PaymentActivity", "Deep link received: $data")
            Log.d("PaymentActivity", "Deep link host: ${data.host}")
            
            when (data.host) {
                "paypal.return" -> {
                    // Log all query parameters
                    val queryParams = data.queryParameterNames
                    Log.d("PaymentActivity", "Query parameters: $queryParams")
                    queryParams.forEach { param ->
                        Log.d("PaymentActivity", "  $param = ${data.getQueryParameter(param)}")
                    }
                    
                    // Try multiple possible parameter names that PayPal might use
                    val orderId = data.getQueryParameter("token") 
                        ?: data.getQueryParameter("orderId")
                        ?: data.getQueryParameter("id")
                        ?: data.getQueryParameter("orderID")
                    
                    if (orderId != null) {
                        Log.d("PaymentActivity", "Attempting to capture order: $orderId")
                        capturePayPalOrder(orderId)
                    } else {
                        // Log what parameters were actually received
                        val receivedParams = queryParams.joinToString(", ")
                        Log.e("PaymentActivity", "No order ID found in parameters: $receivedParams")
                        showPaymentFailure("Invalid payment token - no order ID found in redirect URL")
                    }
                }
                "paypal.cancel" -> {
                    // 用户取消支付
                    Log.d("PaymentActivity", "Payment cancelled by user")
                    showPaymentFailure("Payment was cancelled by user")
                }
                else -> {
                    Log.w("PaymentActivity", "Unknown deep link host: ${data.host}")
                }
            }
        } else {
            if (data != null) {
                Log.w("PaymentActivity", "Deep link with unexpected scheme: ${data.scheme}")
            }
        }
    }

    private fun startPayPalCheckout() {
        lifecycleScope.launch {
            try {
                isProcessingPayment.value = true
                
                val authResult = paypalRepository.authenticate()
                if (authResult.isFailure) {
                    isProcessingPayment.value = false
                    showPaymentFailure("Authentication failed: ${authResult.exceptionOrNull()?.message}")
                    return@launch
                }

                // Create order
                val orderResult = paypalRepository.createOrder(
                    amount = "70.00",
                    currencyCode = "MYR",
                    description = "MUSIC FIESTA 6.0 - 2 Tickets"
                )

                if (orderResult.isSuccess) {
                    val order = orderResult.getOrNull()!!
                    pendingOrderId = order.id

                    // Find the approve link
                    val approveLink = order.links.find { it.rel == "approve" }

                    if (approveLink != null) {
                        Log.d("PaymentScreen", "Opening PayPal checkout URL: ${approveLink.href}")
                        isProcessingPayment.value = false
                        
                        // Open PayPal checkout page in Custom Tab
                        val customTabsIntent = CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()

                        customTabsIntent.launchUrl(this@PaymentActivity, Uri.parse(approveLink.href))
                    } else {
                        isProcessingPayment.value = false
                        showPaymentFailure("Could not get PayPal checkout URL")
                    }
                } else {
                    isProcessingPayment.value = false
                    showPaymentFailure("Order creation failed: ${orderResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                isProcessingPayment.value = false
                showPaymentFailure("Error: ${e.message}")
            }
        }
    }

    private fun capturePayPalOrder(orderId: String) {
        isProcessingPayment.value = true
        lifecycleScope.launch {
            try {
                val captureResult = paypalRepository.captureOrder(orderId)

                if (captureResult.isSuccess) {
                    val capture = captureResult.getOrNull()!!
                    val reservation = viewModel.reservation.value
                    
                    // Save payment to Firebase
                    if (reservation != null) {
                        val payment = com.example.sera_application.domain.model.Payment(
                            paymentId = capture.id,
                            userId = reservation.userId,
                            eventId = reservation.eventId,
                            reservationId = reservation.reservationId,
                            amount = reservation.totalPrice,
                            status = PaymentStatus.SUCCESS,
                            createdAt = System.currentTimeMillis()
                        )
                        
                        // Save payment to Firebase
                        try {
                            Log.d("PaymentActivity", "Attempting to save payment: $payment")
                            val savedPaymentId = paymentRemoteDataSource.savePayment(payment)
                            Log.d("PaymentActivity", "✅ Payment saved to Firebase successfully: $savedPaymentId")
                        } catch (e: Exception) {
                            Log.e("PaymentActivity", "❌ Error saving payment to Firebase: ${e.message}", e)
                            e.printStackTrace()
                        }
                        
                        // Update reservation status to CONFIRMED
                        // TODO: Add UpdateReservationStatusUseCase call here
                    }

                    // Navigate to success screen
                    val intent = Intent(this@PaymentActivity, PaymentStatusActivity::class.java).apply {
                        putExtra("PAYMENT_SUCCESS", true)
                        putExtra("TRANSACTION_ID", capture.id)
                        putExtra("AMOUNT", reservation?.totalPrice ?: 70.0)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    // Navigate to failure screen
                    showPaymentFailure(captureResult.exceptionOrNull()?.message ?: "Payment capture failed")
                }
            } catch (e: Exception) {
                showPaymentFailure(e.message ?: "Unknown error occurred")
            } finally {
                isProcessingPayment.value = false
            }
        }
    }

    private fun showPaymentFailure(reason: String) {
        val intent = Intent(this@PaymentActivity, PaymentStatusActivity::class.java).apply {
            putExtra("PAYMENT_SUCCESS", false)
            putExtra("FAILURE_REASON", reason)
            putExtra("AMOUNT", 70.0)
        }
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    reservationId: String,
    reservation: EventReservation?,
    event: Event?,
    isLoadingData: Boolean,
    onBack: () -> Unit,
    onPaymentSuccess: (String) -> Unit,
    isProcessing: Boolean,
    onPayPalCheckout: () -> Unit
) {
    val context = LocalContext.current
    var selectedPayment by rememberSaveable { mutableStateOf("") }
    var showPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Payment",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!isProcessing) {
                                onBack()
                            }
                        },
                        enabled = !isProcessing
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2D2D2D)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoadingData) {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF5B9FED))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    EventDetailsCard(event = event, reservation = reservation)

                    Spacer(modifier = Modifier.height(16.dp))

                    PriceBreakdownCard(reservation = reservation)

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Payment Method",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // PayPal Option
                    PaymentMethodCard(
                        title = "PayPal",
                        icon = R.drawable.ic_launcher_foreground,
                        selected = selectedPayment == "PayPal",
                        onSelect = {
                            selectedPayment = "PayPal"
                        },
                        enabled = !isProcessing
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (selectedPayment.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Please select a payment method",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showPaymentDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B9FED)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "Proceed to Payment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Loading overlay
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF5B9FED),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Processing Payment...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please wait",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        PaymentDetailsDialog(
            email = email,
            name = name,
            paymentMethod = selectedPayment,
            onEmailChange = { email = it },
            onNameChange = { name = it },
            onDismiss = { showPaymentDialog = false },
            onProceedToPayPal = {
                showPaymentDialog = false
                onPayPalCheckout()
            }
        )
    }
}

@Composable
fun EventDetailsCard(event: Event?, reservation: EventReservation?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.eventdeafault),
                    contentDescription = "Event Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = event?.name ?: "Event Name",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            EventDetailRow("Date", event?.date?.let { 
                val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                format.format(java.util.Date(it))
            } ?: "N/A")
            Spacer(modifier = Modifier.height(6.dp))
            EventDetailRow("Time", event?.timeRange ?: "N/A")
            Spacer(modifier = Modifier.height(6.dp))
            EventDetailRow("Venue", event?.location ?: "N/A")
            Spacer(modifier = Modifier.height(6.dp))
            EventDetailRow("Ticket", "${reservation?.seats ?: 0} x Tickets")
        }
    }
}

@Composable
fun EventDetailRow(label: String, value: Any) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF757575),
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun PriceBreakdownCard(reservation: EventReservation?) {
    val seats = reservation?.seats ?: 0
    val pricePerTicket = if (seats > 0) (reservation?.totalPrice ?: 0.0) / seats else 0.0
    val totalPrice = reservation?.totalPrice ?: 0.0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subtotal",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                Text(
                    text = "$seats x RM %.2f".format(pricePerTicket),
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Color(0xFFE5E5E5))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Price",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "RM%.2f".format(totalPrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    title: String,
    icon: Int,
    selected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                alpha = if (enabled) 1f else 0.5f
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.Black else Color.Gray,
                modifier = Modifier.weight(1f)
            )

            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.Black,
                    unselectedColor = Color.Gray,
                    disabledSelectedColor = Color.Gray,
                    disabledUnselectedColor = Color.LightGray
                )
            )
        }
    }
}

@Composable
fun PaymentDetailsDialog(
    email: String,
    name: String,
    paymentMethod: String,
    onEmailChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onProceedToPayPal: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Payment Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    placeholder = {
                        Text(
                            "haha@gmail.com",
                            color = Color(0xFFBDBDBD)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.DarkGray,
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    placeholder = {
                        Text(
                            "Lim Siau Siau",
                            color = Color(0xFFBDBDBD)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.DarkGray,
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (paymentMethod == "PayPal") {
                            "Your financial information is protected by PayPal"
                        } else {
                            "Your card will be processed securely by PayPal"
                        },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B9FED)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        if (email.isBlank() || name.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please fill in all fields",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                        if (!email.matches(emailPattern.toRegex())) {
                            Toast.makeText(
                                context,
                                "Please enter a valid email",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        onProceedToPayPal()
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
                        text = when (paymentMethod) {
                            "PayPal" -> "Continue with PayPal"
                            "Credit Card" -> "Pay with Credit Card"
                            "Debit Card" -> "Pay with Debit Card"
                            else -> "Continue"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = null
    )
}