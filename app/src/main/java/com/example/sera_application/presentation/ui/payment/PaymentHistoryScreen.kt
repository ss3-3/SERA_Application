package com.example.sera_application.presentation.ui.payment

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.sera_application.presentation.navigation.LocalNavigationController
import com.example.sera_application.presentation.navigation.NavigationController
import com.example.sera_application.ui.theme.SERA_ApplicationTheme
import com.example.sera_application.utils.PdfReceiptGenerator
import com.example.sera_application.utils.ReceiptData
import com.example.sera_application.utils.BottomNavigationBar
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.presentation.viewmodel.payment.PaymentHistoryViewModel
import com.example.sera_application.utils.BottomNavigationBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PaymentHistoryActivity : ComponentActivity() {
    private var viewModel: PaymentHistoryViewModel? = null
    private var currentUserId: String? = null

    @Inject
    lateinit var getPaymentByIdUseCase: com.example.sera_application.domain.usecase.payment.GetPaymentByIdUseCase
    
    @Inject
    lateinit var getReservationByIdUseCase: com.example.sera_application.domain.usecase.reservation.GetReservationByIdUseCase
    
    @Inject
    lateinit var getEventByIdUseCase: com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
    
    @Inject
    lateinit var getUserProfileUseCase: com.example.sera_application.domain.usecase.user.GetUserProfileUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if this activity was started to generate PDF
        val generatePdf = intent.getBooleanExtra("GENERATE_PDF", false)
        if (generatePdf) {
            val orderId = intent.getStringExtra("ORDER_ID") ?: ""
            val reservationId = intent.getStringExtra("RESERVATION_ID")
            val eventName = intent.getStringExtra("EVENT_NAME") ?: "Event"
            val amount = intent.getDoubleExtra("AMOUNT", 0.0)
            val date = intent.getStringExtra("DATE") ?: ""
            val tickets = intent.getStringExtra("TICKETS") ?: "1 ticket"
            
            val orderData = OrderData(
                eventName = eventName,
                orderId = orderId,
                price = "RM ${String.format(Locale.US, "%.2f", amount)}",
                tickets = tickets,
                status = "SUCCESS",
                date = date,
                reservationId = reservationId
            )
            
            // Generate PDF and finish activity after completion
            // Don't return immediately - let the activity stay alive for PDF generation
            generateAndOpenPdf(orderData) {
                // Finish activity after PDF is opened (with a small delay to ensure PDF viewer opens)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
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
        
        setContent {
            SERA_ApplicationTheme {
                val vm: PaymentHistoryViewModel = hiltViewModel()
                viewModel = vm

                PaymentHistoryScreen(
                    onViewReceipt = { orderData ->
                        generateAndOpenPdf(orderData)
                    },
                    viewModel = vm,
                    onUserIdLoaded = { userId ->
                        currentUserId = userId
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh payment history when returning from refund request
        currentUserId?.let { userId ->
            viewModel?.loadPaymentHistory(userId)
        }
    }

    private fun generateAndOpenPdf(orderData: OrderData, onComplete: (() -> Unit)? = null) {
        lifecycleScope.launch {
            try {
                android.util.Log.d("PaymentHistory", "Starting PDF generation for order: ${orderData.orderId}")
                Toast.makeText(
                    this@PaymentHistoryActivity,
                    "Generating receipt...",
                    Toast.LENGTH_SHORT
                ).show()

                val pdfFile: java.io.File = withContext(Dispatchers.IO) {
                    try {
                        android.util.Log.d("PaymentHistory", "Loading payment data for: ${orderData.orderId}")
                        
                        // Try to load payment data, but use fallback if it fails
                        var payment: com.example.sera_application.domain.model.Payment? = null
                        var reservation: com.example.sera_application.domain.model.EventReservation? = null
                        var event: com.example.sera_application.domain.model.Event? = null
                        var user: com.example.sera_application.domain.model.User? = null
                        
                        try {
                            payment = getPaymentByIdUseCase(orderData.orderId)
                            android.util.Log.d("PaymentHistory", "Payment loaded: ${payment != null}")
                        } catch (e: Exception) {
                            android.util.Log.w("PaymentHistory", "Failed to load payment: ${e.message}")
                        }
                        
                        // Load reservation if available
                        try {
                            reservation = orderData.reservationId?.let { 
                                android.util.Log.d("PaymentHistory", "Loading reservation: $it")
                                getReservationByIdUseCase(it) 
                            }
                            android.util.Log.d("PaymentHistory", "Reservation loaded: ${reservation != null}")
                        } catch (e: Exception) {
                            android.util.Log.w("PaymentHistory", "Failed to load reservation: ${e.message}")
                        }
                        
                        // Load event
                        try {
                            event = payment?.let { 
                                android.util.Log.d("PaymentHistory", "Loading event: ${it.eventId}")
                                getEventByIdUseCase(it.eventId) 
                            }
                            android.util.Log.d("PaymentHistory", "Event loaded: ${event != null}")
                        } catch (e: Exception) {
                            android.util.Log.w("PaymentHistory", "Failed to load event: ${e.message}")
                        }
                        
                        // Load user
                        try {
                            user = payment?.let { 
                                android.util.Log.d("PaymentHistory", "Loading user: ${it.userId}")
                                getUserProfileUseCase(it.userId) 
                            }
                            android.util.Log.d("PaymentHistory", "User loaded: ${user != null}")
                        } catch (e: Exception) {
                            android.util.Log.w("PaymentHistory", "Failed to load user: ${e.message}")
                        }
                        
                        // Format date and time
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val paymentDate = payment?.let { Date(it.createdAt) } ?: Date()
                        
                        // Calculate ticket quantity
                        val quantity = reservation?.let { 
                            (it.rockZoneSeats ?: 0) + (it.normalZoneSeats ?: 0)
                        } ?: orderData.tickets.split(" ")[0].toIntOrNull() ?: 1
                        
                        android.util.Log.d("PaymentHistory", "Creating ReceiptData with quantity: $quantity")
                        val receiptData = ReceiptData(
                            eventName = event?.name ?: orderData.eventName,
                            transactionId = orderData.orderId,
                            date = payment?.let { dateFormat.format(Date(it.createdAt)) } ?: orderData.date,
                            time = payment?.let { timeFormat.format(Date(it.createdAt)) } ?: "7:00 PM",
                            venue = event?.location ?: "Unknown",
                            ticketType = "NORMAL",
                            quantity = quantity,
                            seats = "N/A", // Could be enhanced to show actual seats
                            price = payment?.amount ?: orderData.price.replace("RM ", "").toDoubleOrNull() ?: 70.0,
                            email = user?.email ?: "",
                            name = user?.fullName ?: "",
                            phone = user?.phone ?: ""
                        )

                        android.util.Log.d("PaymentHistory", "Generating PDF...")
                        val generator = PdfReceiptGenerator(this@PaymentHistoryActivity)
                        val file = generator.generateReceipt(receiptData)
                        android.util.Log.d("PaymentHistory", "PDF generated successfully: ${file.absolutePath}")
                        file
                    } catch (e: Exception) {
                        android.util.Log.e("PaymentHistory", "Error in PDF generation: ${e.message}", e)
                        e.printStackTrace()
                        throw e
                    }
                }

                android.util.Log.d("PaymentHistory", "Creating FileProvider URI...")
                val uri = FileProvider.getUriForFile(
                    this@PaymentHistoryActivity,
                    "${applicationContext.packageName}.fileprovider",
                    pdfFile
                )

                android.util.Log.d("PaymentHistory", "Opening PDF viewer...")
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                startActivity(intent)
                android.util.Log.d("PaymentHistory", "PDF opened successfully")
                
                // Call completion callback if provided
                onComplete?.invoke()

            } catch (e: Exception) {
                android.util.Log.e("PaymentHistory", "Error generating receipt: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(
                    this@PaymentHistoryActivity,
                    "Error generating receipt: ${e.message}\n${e.javaClass.simpleName}",
                    Toast.LENGTH_LONG
                ).show()
                
                // Still call completion callback even on error
                onComplete?.invoke()
            }
        }
    }
}

/**
 * Data class representing order/payment information for display in PaymentHistoryScreen.
 * This is used to pass order data between composables.
 */
data class OrderData(
    val eventName: String,
    val orderId: String,
    val price: String,
    val tickets: String,
    val status: String,
    val date: String,
    val reservationId: String? = null // Added to support navigation to ReceiptActivity
)

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF5B9FED) else Color.White,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onViewReceipt: (OrderData) -> Unit,
    navigationController: NavigationController = LocalNavigationController.current,
    navController: androidx.navigation.NavController? = null,
    viewModel: PaymentHistoryViewModel = hiltViewModel(),
    onUserIdLoaded: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    // Get current user for role-based navigation
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val currentUser by profileViewModel.user.collectAsState()

    // Payment history data from ViewModel
    val filteredOrders by viewModel.filteredOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedFilter by rememberSaveable { mutableStateOf("All") }

    // Load current user and payment history
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }

    LaunchedEffect(currentUser) {
        currentUser?.userId?.let { userId ->
            onUserIdLoaded?.invoke(userId)
            viewModel.loadPaymentHistory(userId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigationController.navigateBack() }) {
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
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Summary",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Card with real data
            val totalPayments = filteredOrders.size
            val successfulPayments = filteredOrders.count { it.status.contains("Paid", ignoreCase = true) }
            val refundedPayments = filteredOrders.count { it.status.contains("Refund", ignoreCase = true) }
            val totalSpent = filteredOrders
                .filter { it.status.contains("Paid", ignoreCase = true) }
                .sumOf {
                    it.price.replace("RM ", "").replace(",", "").toDoubleOrNull() ?: 0.0
                }

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
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Payments",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$totalPayments",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Successful",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$successfulPayments",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Refunded",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$refundedPayments",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Spent",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "RM ${String.format("%.2f", totalSpent)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterButton(
                    text = "All",
                    isSelected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" }
                )
                FilterButton(
                    text = "Success",
                    isSelected = selectedFilter == "Success",
                    onClick = { selectedFilter = "Success" }
                )
                FilterButton(
                    text = "Failed",
                    isSelected = selectedFilter == "Failed",
                    onClick = { selectedFilter = "Failed" }
                )
                FilterButton(
                    text = "Refunded",
                    isSelected = selectedFilter == "Refunded",
                    onClick = { selectedFilter = "Refunded" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apply additional filter based on selected button
            val displayedOrders = filteredOrders.filter { order ->
                val statusUpper = order.status.uppercase()
                when (selectedFilter) {
                    "All" -> true
                    "Success" -> (statusUpper == "SUCCESS" || statusUpper == "PAID") &&
                            !statusUpper.contains("REFUND")
                    "Failed" -> statusUpper == "FAILED" || statusUpper.contains("CANCEL", ignoreCase = true)
                    "Refunded" -> statusUpper.contains("REFUND")
                    else -> true
                }
            }

            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (displayedOrders.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No orders found",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                displayedOrders.forEach { order ->
                    OrderCard(
                        orderData = order,
                        onViewReceipt = { onViewReceipt(order) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}


@Composable
fun OrderCard(
    orderData: OrderData,
    onViewReceipt: (OrderData) -> Unit
) {
    val context = LocalContext.current
    var showDetailsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = orderData.eventName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = orderData.orderId,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${orderData.price} • ${orderData.tickets}",
                fontSize = 14.sp,
                color = Color.Black
            )

            if (orderData.status == "Refund Pending") {
                Text(
                    text = orderData.status,
                    fontSize = 14.sp,
                    color = Color(0xFFFFA726),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Requested on ${orderData.date}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            } else {
                Row {
                    Text(
                        text = orderData.status,
                        fontSize = 14.sp,
                        color = when (orderData.status) {
                            "Paid" -> Color(0xFF4CAF50)
                            "Refunded" -> Color(0xFF2196F3)
                            else -> Color.Black
                        },
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • ${orderData.date}",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusUpper = orderData.status.uppercase()
                val isPaid = statusUpper == "SUCCESS" || statusUpper == "PAID"
                val isRefundPending = statusUpper == "REFUND_PENDING" || statusUpper.contains("REFUND PENDING", ignoreCase = true)
                val isRefunded = statusUpper == "REFUNDED"


                // If status is Refund Pending or Refunded: Show only "View Details" button
                if (isRefundPending || isRefunded) {
                    OutlinedButton(
                        onClick = {
                            // Show order details dialog
                            showDetailsDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text("View Details", fontSize = 14.sp)
                    }
                }
                // If status is Paid: Show "View Receipt" and "Request Refund" buttons (two buttons, NO View Details)
                else if (isPaid) {
                    OutlinedButton(
                        onClick = {
                            // Generate and open PDF directly (same as download receipt function)
                            onViewReceipt(orderData)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text("View Receipt", fontSize = 14.sp)
                    }


                    OutlinedButton(
                        onClick = {
                            val intent = Intent(context, RefundRequestActivity::class.java).apply {
                                putExtra("PAYMENT_ID", orderData.orderId)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Request Refund", fontSize = 14.sp)
                    }
                }
                // For other statuses (Failed, etc.): Show only "View Details" button
                else {
                    OutlinedButton(
                        onClick = {
                            // Show order details dialog
                            showDetailsDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text("View Details", fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Order Details Dialog
    if (showDetailsDialog) {
        OrderDetailsDialog(
            orderData = orderData,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun OrderDetailsDialog(
    orderData: OrderData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Order Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Event Name
                DetailRow(
                    label = "Event Name",
                    value = orderData.eventName
                )

                Divider(color = Color(0xFFE0E0E0))

                // Order ID
                DetailRow(
                    label = "Order ID",
                    value = orderData.orderId
                )

                Divider(color = Color(0xFFE0E0E0))

                // Price
                DetailRow(
                    label = "Price",
                    value = orderData.price
                )

                Divider(color = Color(0xFFE0E0E0))

                // Tickets
                DetailRow(
                    label = "Tickets",
                    value = orderData.tickets
                )

                Divider(color = Color(0xFFE0E0E0))

                // Status
                DetailRow(
                    label = "Status",
                    value = orderData.status,
                    statusColor = when (orderData.status.uppercase()) {
                        "SUCCESS", "PAID" -> Color(0xFF4CAF50)
                        "REFUNDED" -> Color(0xFF2196F3)
                        "REFUND_PENDING", "REFUND PENDING" -> Color(0xFFFFA726)
                        "FAILED" -> Color(0xFFF44336)
                        else -> Color.Black
                    }
                )

                Divider(color = Color(0xFFE0E0E0))

                // Date
                DetailRow(
                    label = "Date",
                    value = orderData.date
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF2D2D2D))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    statusColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
            color = statusColor ?: Color.Black
        )
    }
}