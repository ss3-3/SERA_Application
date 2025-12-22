package com.example.sera_application.presentation.ui.event

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sera_application.presentation.viewmodel.event.EventFormViewModel
import com.example.sera_application.presentation.ui.components.SafeImageLoader
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class EventFormData(
    val name: String = "",
//    val date: Long? = null,
//    val time: Long? = null,
    val dateMillis: Long? = null,     // 📅 picked from DatePicker
    val startTimeMillis: Long? = null, // ⏰ picked from TimePicker
    val endTimeMillis: Long? = null,
    val location: String = "",
    val category: String = "",
    val rockZoneSeats: String = "",
    val normalZoneSeats: String = "",
    val rockZonePrice: Double = 0.0,
    val normalZonePrice: Double = 0.0,
    val description: String = "",
    val imagePath: String? = null // Can be drawable name or local file path
)

data class VenueCapacity(
    val name: String,
    val totalSeats: Int,
    val rockZoneSeats: Int,
    val normalZoneSeats: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    eventId: String? = null,
    isEditMode: Boolean = false,
    initialEventData: EventFormData? = null,
    onBackClick: () -> Unit = {},
    onSubmitClick: (EventFormData) -> Unit = {},
    onImageSelected: ((Uri) -> Unit)? = null,
    currentImagePath: String? = null, // Image path from ViewModel after saving
    isLoading: Boolean = false, // Loading state from ViewModel
    errorMessage: String? = null // Error message from ViewModel
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            onImageSelected?.invoke(it)
        }
    }

    var eventName by remember { mutableStateOf("") }
    var eventDateMillis by remember { mutableStateOf<Long?>(null) }
    var eventDateText by remember { mutableStateOf("") }
//    var eventDuration by remember { mutableStateOf("") }
    var eventStartTime by remember { mutableStateOf("") }
    var eventEndTime by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var rockZoneSeats by remember { mutableStateOf("") }
    var normalZoneSeats by remember { mutableStateOf("") }
    var rockZonePrice by remember { mutableStateOf("") }
    var normalZonePrice by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var locationExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(initialEventData) {
        initialEventData?.let { data ->
            eventName = data.name

            data.dateMillis?.let { millis ->
                eventDateMillis = millis
                eventDateText = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(millis)
            }

            data.startTimeMillis?.let { millis ->
                eventStartTime = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(millis)
            }

            data.endTimeMillis?.let {
                eventEndTime = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(it)
            }

            selectedLocation = data.location
            selectedCategory = data.category
            rockZoneSeats = data.rockZoneSeats
            normalZoneSeats = data.normalZoneSeats
            rockZonePrice =
                if (data.rockZonePrice > 0) "%.2f".format(data.rockZonePrice) else ""
            normalZonePrice =
                if (data.normalZonePrice > 0) "%.2f".format(data.normalZonePrice) else ""
            eventDescription = data.description
        }
    }

    val venueCapacities = listOf(
        VenueCapacity("DSA, TARUMT (300 seats)", 300, 50, 250),
        VenueCapacity("Rimba, TARUMT (400 seats)", 400, 80, 320),
        VenueCapacity("DTAR, TARUMT (700 seats)", 700, 200, 500),
        VenueCapacity("Sport Complex, TARUMT (1000 seats)", 1000, 300, 700),
        VenueCapacity("Arena, TARUMT (1500 seats)", 1000, 500, 1000)
    )

    val locationOptions = venueCapacities.map { it.name }
    val categoryOptions = listOf("Academic", "Career", "Art", "Wellness", "Music", "Festival")

    val screenTitle = if (isEditMode) "Edit Event" else "Create Event"
    val submitButtonText = if (isEditMode) "Save" else "Create"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        // --- DIALOGS ---
        if (showDatePicker) {
            // Initialize with existing date in edit mode
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = eventDateMillis ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            eventDateMillis = millis
                            eventDateText = sdf.format(millis)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showStartTimePicker) {
            // Initialize with existing time in edit mode
            val currentCal = Calendar.getInstance()
            if (initialEventData?.startTimeMillis != null) {
                currentCal.timeInMillis = initialEventData.startTimeMillis
            }
            val timePickerState = rememberTimePickerState(
                initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
                initialMinute = currentCal.get(Calendar.MINUTE)
            )
            AlertDialog(
                onDismissRequest = { showStartTimePicker = false },
                title = { Text("Select Start Time") },
                text = { 
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        cal.set(Calendar.MINUTE, timePickerState.minute)
                        eventStartTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                        showStartTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEndTimePicker) {
            // Initialize with existing time in edit mode
            val currentCal = Calendar.getInstance()
            if (initialEventData?.endTimeMillis != null) {
                currentCal.timeInMillis = initialEventData.endTimeMillis
            }
            val timePickerState = rememberTimePickerState(
                initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
                initialMinute = currentCal.get(Calendar.MINUTE)
            )
            AlertDialog(
                onDismissRequest = { showEndTimePicker = false },
                title = { Text("Select End Time") },
                text = { 
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        cal.set(Calendar.MINUTE, timePickerState.minute)
                        eventEndTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                        showEndTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Event Profile Picture
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    // Prefer currentImagePath (from ViewModel after saving) over initialEventData
                    val imagePath = currentImagePath ?: initialEventData?.imagePath

                    when {
                        selectedImageUri != null -> {
                            // Newly selected image from picker - show immediately
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Event Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        !imagePath.isNullOrBlank() -> {
                            // Use safe loader for existing imagePath (handles null/empty/invalid)
                            SafeImageLoader(
                                imagePath = imagePath,
                                contentDescription = "Event Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            // Show upload placeholder if no image
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Upload Image",
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap to upload image",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Event Name Field
            item {
                FormFieldLabel("Event name", isRequired = true)
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    placeholder = { Text("Enter event name", fontSize = 14.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF2196F3)
                    ),
                    singleLine = true
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Date and Duration Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Date", isRequired = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                        ) {
                        OutlinedTextField(
                            value = eventDateText,
                            onValueChange = {},
                            readOnly = true,
                                enabled = false,
                            placeholder = { Text("Select date", fontSize = 14.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFFE0E0E0),
                                    disabledTextColor = Color.Black,
                                    disabledPlaceholderColor = Color.Gray,
                                    disabledTrailingIconColor = Color.Gray
                            ),
                                trailingIcon = { 
                                    Icon(
                                        Icons.Default.CalendarToday, 
                                        "Select Date",
                                        tint = Color(0xFF2196F3)
                                    ) 
                                },
                            singleLine = true
                        )
                        }
                    }

//                    Column(modifier = Modifier.weight(1f)) {
//                        FormFieldLabel("Duration", isRequired = true)
//                        OutlinedTextField(
//                            value = eventDuration,
//                            onValueChange = { eventDuration = it },
//                            placeholder = { Text("Enter duration", fontSize = 14.sp, color = Color.Gray) },
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(8.dp),
//                            colors = OutlinedTextFieldDefaults.colors(
//                                unfocusedBorderColor = Color(0xFFE0E0E0),
//                                focusedBorderColor = Color(0xFF2196F3)
//                            ),
//                            singleLine = true
//                        )
//                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Time Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Start Time", isRequired = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStartTimePicker = true }
                        ) {
                        OutlinedTextField(
                            value = eventStartTime,
                            onValueChange = {},
                            readOnly = true,
                                enabled = false,
                            placeholder = { Text("Select time", fontSize = 14.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFFE0E0E0),
                                    disabledTextColor = Color.Black,
                                    disabledPlaceholderColor = Color.Gray,
                                    disabledTrailingIconColor = Color.Gray
                            ),
                                trailingIcon = { 
                                    Icon(
                                        Icons.Default.AccessTime, 
                                        "Select Time",
                                        tint = Color(0xFF2196F3)
                                    ) 
                                },
                            singleLine = true
                        )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("End Time", isRequired = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEndTimePicker = true }
                        ) {
                        OutlinedTextField(
                            value = eventEndTime,
                            onValueChange = {},
                            readOnly = true,
                                enabled = false,
                            placeholder = { Text("Select time", fontSize = 14.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFFE0E0E0),
                                    disabledTextColor = Color.Black,
                                    disabledPlaceholderColor = Color.Gray,
                                    disabledTrailingIconColor = Color.Gray
                            ),
                                trailingIcon = { 
                                    Icon(
                                        Icons.Default.AccessTime, 
                                        "Select Time",
                                        tint = Color(0xFF2196F3)
                                    ) 
                                },
                            singleLine = true
                        )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Category Dropdown
            item {
                FormFieldLabel("Category", isRequired = true)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select a category", fontSize = 14.sp, color = Color.Gray) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF2196F3)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categoryOptions.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, fontSize = 14.sp) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Location Dropdown
            item {
                FormFieldLabel("Location", isRequired = true)
                ExposedDropdownMenuBox(
                    expanded = locationExpanded,
                    onExpandedChange = { locationExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select a location", fontSize = 14.sp, color = Color.Gray) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF2196F3)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        locationOptions.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location, fontSize = 14.sp) },
                                onClick = {
                                    selectedLocation = location
                                    locationExpanded = false

                                    val selectedVenue = venueCapacities.find { it.name == location }
                                    if (selectedVenue != null) {
                                        rockZoneSeats = selectedVenue.rockZoneSeats.toString()
                                        normalZoneSeats = selectedVenue.normalZoneSeats.toString()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Seats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Rock zone seats", isRequired = true)
                        OutlinedTextField(
                            value = rockZoneSeats,
                            onValueChange = { rockZoneSeats = it },
                            placeholder = { Text("Seats quantity", fontSize = 14.sp, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color(0xFFE0E0E0),
                                disabledContainerColor = Color(0xFFF5F5F5)
                            ),
                            singleLine = true,
                            readOnly = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Normal zone seats", isRequired = true)
                        OutlinedTextField(
                            value = normalZoneSeats,
                            onValueChange = { normalZoneSeats = it },
                            placeholder = { Text("Seats quantity", fontSize = 14.sp, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color(0xFFE0E0E0),
                                disabledContainerColor = Color(0xFFF5F5F5)
                            ),
                            singleLine = true,
                            readOnly = true
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Price
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Rock zone price", isRequired = true)
                        OutlinedTextField(
                            value = rockZonePrice,
                            onValueChange = { input ->
                                if (input.isEmpty()) {
                                    rockZonePrice = ""
                                } else {
                                    val cleaned = input.filter { it.isDigit() || it == '.' }
                                    if (cleaned.matches(Regex("^\\d+\\.?\\d{0,2}$"))) {
                                        rockZonePrice = cleaned
                                    }
                                }
                            },
                            placeholder = { Text("0.00", fontSize = 14.sp, color = Color.Gray) },
                            leadingIcon = { Text("RM", fontSize = 14.sp, color = Color.Black) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFF2196F3)
                            ),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Normal zone price", isRequired = true)
                        OutlinedTextField(
                            value = normalZonePrice,
                            onValueChange = { input ->
                                if (input.isEmpty()) {
                                    normalZonePrice = ""
                                } else {
                                    val cleaned = input.filter { it.isDigit() || it == '.' }
                                    if (cleaned.matches(Regex("^\\d+\\.?\\d{0,2}$"))) {
                                        normalZonePrice = cleaned
                                    }
                                }
                            },
                            placeholder = { Text("0.00", fontSize = 14.sp, color = Color.Gray) },
                            leadingIcon = { Text("RM", fontSize = 14.sp, color = Color.Black) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFF2196F3)
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Description Field
            item {
                FormFieldLabel("Description", isRequired = true)
                OutlinedTextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    placeholder = { Text("Enter description", fontSize = 14.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF2196F3)
                    ),
                    maxLines = 5
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Submit Button
            item {
                var showLocalError by remember { mutableStateOf(false) }
                var localErrorMessage by remember { mutableStateOf("") }

                // Show ViewModel error message if present
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, "Warning", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage, fontSize = 13.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }

                // Show local validation error if present
                if (showLocalError) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, "Warning", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localErrorMessage, fontSize = 13.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }

                Button(
                    onClick = {
                        // Clear previous errors
                        showLocalError = false
                        localErrorMessage = ""
                        
                        // Validate form fields
                        when {
                            eventName.isBlank() -> { 
                                showLocalError = true
                                localErrorMessage = "Please enter event name"
                            }
                            eventDateMillis == null -> {
                                showLocalError = true
                                localErrorMessage = "Please select event date"
                            }
                            eventStartTime.isBlank() || eventEndTime.isBlank() -> { 
                                showLocalError = true
                                localErrorMessage = "Please select a start and end time"
                            }
                            selectedCategory.isBlank() -> { 
                                showLocalError = true
                                localErrorMessage = "Please select a category"
                            }
                            selectedLocation.isBlank() -> { 
                                showLocalError = true
                                localErrorMessage = "Please select a location"
                            }
                            rockZonePrice.isBlank() || rockZonePrice.toDoubleOrNull() == null -> { 
                                showLocalError = true
                                localErrorMessage = "Please enter valid rock zone price"
                            }
                            normalZonePrice.isBlank() || normalZonePrice.toDoubleOrNull() == null -> { 
                                showLocalError = true
                                localErrorMessage = "Please enter valid normal zone price"
                            }
                            eventDescription.isBlank() -> { 
                                showLocalError = true
                                localErrorMessage = "Please enter event description"
                            }
                            else -> {
                                // Validate time parsing before submitting
                                val parsedStartTime = parseTime(eventDateMillis, eventStartTime)
                                val parsedEndTime = parseTime(eventDateMillis, eventEndTime)
                                
                                if (parsedStartTime == null) {
                                    showLocalError = true
                                    localErrorMessage = "Invalid start time format. Please select the time again."
                                } else if (parsedEndTime == null) {
                                    showLocalError = true
                                    localErrorMessage = "Invalid end time format. Please select the time again."
                                } else if (parsedEndTime <= parsedStartTime) {
                                    showLocalError = true
                                    localErrorMessage = "End time must be after start time"
                                } else {
                                    showLocalError = false
                                    val formData = EventFormData(
                                        name = eventName,
                                        dateMillis = eventDateMillis,
                                        startTimeMillis = parsedStartTime,
                                        endTimeMillis = parsedEndTime,
                                        location = selectedLocation,
                                        category = selectedCategory,
                                        rockZoneSeats = rockZoneSeats,
                                        normalZoneSeats = normalZoneSeats,
                                        rockZonePrice = rockZonePrice.toDoubleOrNull() ?: 0.0,
                                        normalZonePrice = normalZonePrice.toDoubleOrNull() ?: 0.0,
                                        description = eventDescription,
                                        imagePath = currentImagePath ?: initialEventData?.imagePath
                                    )
                                    onSubmitClick(formData)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading, // Disable button when loading
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        disabledContainerColor = Color(0xFFBDBDBD)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Text(submitButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

private fun parseTime(dateMillis: Long?, timeStr: String): Long? {
    if (dateMillis == null || timeStr.isBlank()) return null

    val dateCal = Calendar.getInstance().apply {
        timeInMillis = dateMillis
    }

    val parsedTime = SimpleDateFormat(
        "hh:mm a",
        Locale.getDefault()
    ).parse(timeStr) ?: return null

    val timeCal = Calendar.getInstance().apply {
        time = parsedTime
    }

    return Calendar.getInstance().apply {
        set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
        set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Composable
private fun FormFieldLabel(text: String, isRequired: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        if (isRequired) {
            Text(" *", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// ==================== WRAPPER SCREENS ====================

@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit = {},
    onCreateClick: (EventFormData) -> Unit = {},
    onImageSelected: ((Uri) -> Unit)? = null
) {
    EventFormScreen(
        isEditMode = false,
        initialEventData = null,
        onBackClick = onBackClick,
        onSubmitClick = onCreateClick,
        onImageSelected = onImageSelected
    )
}

@Composable
fun EditEventScreen(
    eventId: String,
    viewModel: EventFormViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    // Automatically navigate back on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetSuccessState() // Reset the flag to prevent re-navigation
            onBackClick()
        }
    }

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage!!)
            }
        }
        else -> {
            val formData = viewModel.getEventFormData()
            EventFormScreen(
                isEditMode = true,
                initialEventData = formData,
                onBackClick = onBackClick,
                onSubmitClick = { submittedForm ->
                    viewModel.submitEvent(
                        formData = submittedForm,
                        isEditMode = true
                    )
                },
                onImageSelected = { uri ->
                    viewModel.onImageSelected(uri)
                },
                currentImagePath = uiState.imagePath,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CreateEventScreenPreview() {
    CreateEventScreen()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditEventScreenPreview() {
    EditEventScreen(eventId = "1")
}