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
import com.example.sera_application.utils.bottomNavigationBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.presentation.viewmodel.payment.PaymentHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SERA_ApplicationTheme {
                PaymentHistoryScreen(
                    onViewReceipt = { orderData ->
                        generateAndOpenPdf(orderData)
                    }
                )
            }
        }
    }

    private fun generateAndOpenPdf(orderData: OrderData) {
        lifecycleScope.launch {
            try {
                Toast.makeText(
                    this@PaymentHistoryActivity,
                    "Generating receipt...",
                    Toast.LENGTH_SHORT
                ).show()

                val pdfFile: java.io.File = withContext(Dispatchers.IO) {
                    val receiptData = ReceiptData(
                        eventName = orderData.eventName,
                        transactionId = orderData.orderId,
                        date = orderData.date,
                        time = "7:00 PM",
                        venue = "Rimba, TARUMT",
                        ticketType = "NORMAL",
                        quantity = orderData.tickets.split(" ")[0].toIntOrNull() ?: 2,
                        seats = "H15, H16",
                        price = orderData.price.replace("RM ", "").toDoubleOrNull() ?: 70.0,
                        email = "haha@gmail.com",
                        name = "Lim Siau Siau",
                        phone = "+60 123456789"
                    )

                    val generator = PdfReceiptGenerator(this@PaymentHistoryActivity)
                    generator.generateReceipt(receiptData)
                }

                val uri = FileProvider.getUriForFile(
                    this@PaymentHistoryActivity,
                    "${applicationContext.packageName}.fileprovider",
                    pdfFile
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                startActivity(intent)

            } catch (e: Exception) {
                Toast.makeText(
                    this@PaymentHistoryActivity,
                    "Error generating receipt: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
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
    val date: String
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
    viewModel: PaymentHistoryViewModel = hiltViewModel()
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
                    text = "Pending",
                    isSelected = selectedFilter == "Pending",
                    onClick = { selectedFilter = "Pending" }
                )
                FilterButton(
                    text = "Paid",
                    isSelected = selectedFilter == "Paid",
                    onClick = { selectedFilter = "Paid" }
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
                when (selectedFilter) {
                    "All" -> true
                    "Pending" -> order.status.contains("Pending", ignoreCase = true)
                    "Paid" -> order.status.contains("Paid", ignoreCase = true) && !order.status.contains("Refund", ignoreCase = true)
                    "Refunded" -> order.status.contains("Refund", ignoreCase = true)
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
    onViewReceipt: () -> Unit
) {
    val context = LocalContext.current

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
                OutlinedButton(
                    onClick = {
                        if (orderData.status == "Paid") {
                            onViewReceipt()
                        } else {
                            val intent = Intent(context, ReceiptActivity::class.java)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        if (orderData.status == "Paid") "View Receipt" else "View Details",
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (orderData.status == "Paid") {
                            val intent = Intent(context, RefundRequestActivity::class.java)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Cannot request refund", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black,
                        disabledContentColor = Color.Gray
                    ),
                    enabled = orderData.status == "Paid"
                ) {
                    Text("Request Refund", fontSize = 14.sp)
                }
            }
        }
    }
}