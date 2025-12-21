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
import androidx.compose.foundation.border
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
import com.example.sera_application.utils.bottomNavigationBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.domain.model.enums.UserRole
import androidx.compose.runtime.LaunchedEffect


@AndroidEntryPoint
class PaymentActivity : ComponentActivity() {


    @Inject
    lateinit var paymentRemoteDataSource: PaymentRemoteDataSource

    @Inject
    lateinit var updateReservationStatusUseCase: com.example.sera_application.domain.usecase.reservation.UpdateReservationStatusUseCase


    private lateinit var paypalRepository: PayPalRepository
    private var pendingOrderId: String? = null
    private val isProcessingPayment = mutableStateOf(false)
    private val viewModel: PaymentScreenViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        paypalRepository = PayPalRepository(
            clientId = "AQTPtN2werWX-j1tUfqQwifM0cfqviYHUVl9exM5fj4Ac2-kYXpqyjuaWw9mya3Tiwe2ppXGYyHNcBAP",
            clientSecret = "EN02FWz7AC_SwRw6FuprITB4AT_XdM2ZMV2p1VSaBY7TJr-gONuIupplRCxQURSxBrMcPDmjxeUDfQf9",
            isSandbox = true
        )


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
                    onBack = {
                        // Navigate back to Create Reservation screen
                        val eventId = event?.eventId
                        if (eventId != null) {
                            val intent = Intent(this, com.example.sera_application.MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("DESTINATION", "create_reservation")
                                putExtra("EVENT_ID", eventId)
                            }
                            startActivity(intent)
                        }
                        finish()
                    },
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

                    val orderId = data.getQueryParameter("token")
                        ?: data.getQueryParameter("orderId")
                        ?: data.getQueryParameter("id")
                        ?: data.getQueryParameter("orderID")


                    // Also extract our custom reservationId to avoid state loss
                    val reservationId = data.getQueryParameter("reservationId")

                    lifecycleScope.launch {
                        if (reservationId != null && viewModel.reservation.value == null) {
                            Log.d("PaymentActivity", "State lost. Reloading reservation: $reservationId")
                            viewModel.loadReservationDetails(reservationId)

                            // Simple wait loop to ensure data is loaded before proceeding to capture
                            // This prevents the race condition where capture finishes before metadata reloads
                            var checkCount = 0
                            while (viewModel.reservation.value == null && checkCount < 10) {
                                kotlinx.coroutines.delay(500)
                                checkCount++
                                Log.d("PaymentActivity", "Waiting for reservation data load... ${checkCount}")
                            }
                        }

                        if (orderId != null) {
                            Log.d("PaymentActivity", "Attempting to capture order: $orderId")
                            capturePayPalOrder(orderId)
                        } else {
                            val receivedParams = queryParams.joinToString(", ")
                            Log.e("PaymentActivity", "No order ID found in parameters: $receivedParams")
                            showPaymentFailure("Invalid payment token - no order ID found in redirect URL")
                        }
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
                val reservation = viewModel.reservation.value
                val event = viewModel.event.value

                val amountStr = String.format("%.2f", reservation?.totalPrice ?: 70.0)
                val eventDescription = "${event?.name ?: "Event"} - ${reservation?.seats ?: 0} Tickets"
                val reservationId = reservation?.reservationId ?: ""


                val orderResult = paypalRepository.createOrder(
                    amount = amountStr,
                    currencyCode = "MYR",
                    description = eventDescription,
                    reservationId = reservationId
                )


                if (orderResult.isSuccess) {
                    val order = orderResult.getOrNull()!!
                    pendingOrderId = order.id

                    Log.d("PaymentActivity", "✅ Order created successfully: ${order.id}")
                    Log.d("PaymentActivity", "Order status: ${order.status}")
                    Log.d("PaymentActivity", "Number of links: ${order.links.size}")

                    // Log all links for debugging
                    order.links.forEachIndexed { index, link ->
                        Log.d("PaymentActivity", "Link $index: rel='${link.rel}', href='${link.href}'")
                    }


                    // Find the approve link
                    val approveLink = order.links.find { it.rel == "approve" }


                    if (approveLink != null) {
                        Log.d("PaymentActivity", "✅ Found approve link: ${approveLink.href}")
                        Log.d("PaymentActivity", "Opening PayPal checkout URL...")
                        isProcessingPayment.value = false

                        try {
                            // Open PayPal checkout page in Custom Tab
                            val customTabsIntent = CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build()


                            customTabsIntent.launchUrl(this@PaymentActivity, Uri.parse(approveLink.href))
                            Log.d("PaymentActivity", "✅ Custom Tab launched successfully")
                        } catch (e: Exception) {
                            Log.e("PaymentActivity", "❌ Error launching Custom Tab: ${e.message}", e)
                            showPaymentFailure("Could not open PayPal checkout page: ${e.message}")
                        }
                    } else {
                        isProcessingPayment.value = false
                        Log.e("PaymentActivity", "❌ No approve link found in order response")
                        Log.e("PaymentActivity", "Available link types: ${order.links.map { it.rel }}")
                        showPaymentFailure("Could not get PayPal checkout URL")
                    }
                } else {
                    isProcessingPayment.value = false
                    Log.e("PaymentActivity", "❌ Order creation failed: ${orderResult.exceptionOrNull()?.message}")
                    showPaymentFailure("Order creation failed: ${orderResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                isProcessingPayment.value = false
                Log.e("PaymentActivity", "❌ Exception in startPayPalCheckout: ${e.message}", e)
                showPaymentFailure("Error: ${e.message}")
            }
        }
    }


    private fun capturePayPalOrder(orderId: String) {
        Log.d("PaymentActivity", "🔄 Starting capture process for order: $orderId")
        isProcessingPayment.value = true
        lifecycleScope.launch {
            try {
                // Re-authenticate to ensure we have a valid access token
                // The token from order creation may have expired
                Log.d("PaymentActivity", "Re-authenticating with PayPal before capture...")
                val authResult = paypalRepository.authenticate()
                if (authResult.isFailure) {
                    Log.e("PaymentActivity", "❌ Re-authentication failed: ${authResult.exceptionOrNull()?.message}")
                    showPaymentFailure("Authentication failed: ${authResult.exceptionOrNull()?.message}")
                    return@launch
                }
                Log.d("PaymentActivity", "✅ Re-authentication successful")

                Log.d("PaymentActivity", "Calling captureOrder API...")
                val captureResult = paypalRepository.captureOrder(orderId)

                Log.d("PaymentActivity", "Capture result: isSuccess=${captureResult.isSuccess}")


                if (captureResult.isSuccess) {
                    val capture = captureResult.getOrNull()!!
                    Log.d("PaymentActivity", "✅ Payment captured successfully: ${capture.id}")
                    Log.d("PaymentActivity", "Capture status: ${capture.status}")

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
                            Toast.makeText(this@PaymentActivity, "Payment recorded successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("PaymentActivity", "❌ Error saving payment to Firebase: ${e.message}", e)
                            e.printStackTrace()
                            // Show user-visible error
                            Toast.makeText(
                                this@PaymentActivity,
                                "Warning: Payment completed but record save failed. Please contact support if needed.",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // Update reservation status to CONFIRMED
                        try {
                            Log.d("PaymentActivity", "Updating reservation status to CONFIRMED: ${reservation.reservationId}")
                            updateReservationStatusUseCase(reservation.reservationId, "CONFIRMED")
                            Log.d("PaymentActivity", "✅ Reservation status updated successfully")
                        } catch (e: Exception) {
                            Log.e("PaymentActivity", "❌ Error updating reservation status: ${e.message}", e)
                        }
                    }


                    // Navigate to success screen
                    Log.d("PaymentActivity", "Navigating to success screen...")
                    val intent = Intent(this@PaymentActivity, PaymentStatusActivity::class.java).apply {
                        putExtra("PAYMENT_SUCCESS", true)
                        putExtra("TRANSACTION_ID", capture.id)
                        putExtra("AMOUNT", reservation?.totalPrice ?: 70.0)
                        putExtra("RESERVATION_ID", reservation?.reservationId)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    // Navigate to failure screen
                    val errorMessage = captureResult.exceptionOrNull()?.message ?: "Payment capture failed"
                    Log.e("PaymentActivity", "❌ Capture failed: $errorMessage")
                    showPaymentFailure(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("PaymentActivity", "❌ Exception in capturePayPalOrder: ${e.message}", e)
                e.printStackTrace()
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
    onPayPalCheckout: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    val context = LocalContext.current
    var selectedPayment by rememberSaveable { mutableStateOf("") }
    var showPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    
    // Get current user for role-based navigation
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val currentUser by profileViewModel.user.collectAsState()
    
    // Load current user
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }


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
            navController?.let { nav ->
                bottomNavigationBar(
                    navController = nav,
                    currentRoute = nav.currentBackStackEntry?.destination?.route,
                    userRole = currentUser?.role ?: UserRole.PARTICIPANT
                )
            }
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
                PaymentEventImage(
                    imagePath = event?.imagePath,
                    eventName = event?.name ?: "Event",
                    modifier = Modifier.fillMaxSize()
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
fun EventDetailRow(label: String, value: String) {
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
            text = value,
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

@Composable
private fun PaymentEventImage(
    imagePath: String?,
    eventName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Try to load as drawable resource first
    val imageRes = remember(imagePath) {
        if (imagePath != null && imagePath.isNotBlank()) {
            // Check if it's a drawable resource name
            val resId = context.resources.getIdentifier(
                imagePath,
                "drawable",
                context.packageName
            )
            if (resId != 0) resId else null
        } else {
            null
        }
    }
    
    when {
        // Case 1: Valid drawable resource
        imageRes != null -> {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = eventName,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        // Case 2: Try as file path (future support)
        imagePath != null && imagePath.isNotBlank() -> {
            // For now, if not a drawable, use default
            Image(
                painter = painterResource(id = R.drawable.eventdeafault),
                contentDescription = eventName,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        // Case 3: No image - use default
        else -> {
            Image(
                painter = painterResource(id = R.drawable.eventdeafault),
                contentDescription = eventName,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    }
}