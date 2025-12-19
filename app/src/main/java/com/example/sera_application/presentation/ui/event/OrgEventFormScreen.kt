package com.example.sera_application.presentation.ui.event

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File

data class EventFormData(
    val name: String = "",
    val date: String = "",
    val duration: String = "",
    val time: String = "",
    val location: String = "",
    val category: String = "",
    val rockZoneSeats: String = "",
    val normalZoneSeats: String = "",
    val rockZonePrice: Double = 0.0,
    val normalZonePrice: Double = 0.0,
    val description: String = "",
    val imagePath: String? = null // Local file path (e.g. /data/user/0/.../event_123.jpg)
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
    eventId: String? = null, // null for create mode
    isEditMode: Boolean = false,              // TRUE = Edit mode, FALSE = Create mode
    initialEventData: EventFormData? = null,  // Pre-filled data for Edit mode
    onBackClick: () -> Unit = {},
    onSubmitClick: (EventFormData) -> Unit = {},  // Returns form data when submitted
    onImageSelected: ((android.net.Uri) -> Unit)? = null
) {
    // Track selected image URI
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it

            onImageSelected?.invoke(it)
        }
    }

    // Initialize form fields
    // If Edit mode: use initialEventData values, otherwise use empty strings
    var eventName by remember { mutableStateOf(initialEventData?.name ?: "") }
    var eventDate by remember { mutableStateOf(initialEventData?.date ?: "") }
    var eventDuration by remember { mutableStateOf(initialEventData?.duration ?: "") }
    var eventTime by remember { mutableStateOf(initialEventData?.time ?: "") }
    var selectedLocation by remember { mutableStateOf(initialEventData?.location ?: "") }
    var selectedCategory by remember { mutableStateOf(initialEventData?.category ?: "") }
    var rockZoneSeats by remember { mutableStateOf(initialEventData?.rockZoneSeats ?: "") }
    var normalZoneSeats by remember { mutableStateOf(initialEventData?.normalZoneSeats ?: "") }
    // !!!ADD SEAT PRICE!!!
    // Use String for UI state, convert Double to String for edit mode!!!
    var rockZonePrice by remember {
        mutableStateOf(
            if (initialEventData?.rockZonePrice != null && initialEventData.rockZonePrice > 0.0)
                String.format("%.2f", initialEventData.rockZonePrice)
            else ""
        )
    }
    var normalZonePrice by remember {
        mutableStateOf(
            if (initialEventData?.normalZonePrice != null && initialEventData.normalZonePrice > 0.0)
                String.format("%.2f", initialEventData.normalZonePrice)
            else ""
        )
    }
    // !!!ADD SEAT PRICE!!!
    var eventDescription by remember { mutableStateOf(initialEventData?.description ?: "") }
    var locationExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // !!!LIST MAY NEED TO STORE IN VIEW MODEL, predefined venue capacity
    val venueCapacities = listOf(
        VenueCapacity("DSA, TARUMT (300 seats)", 300, 50, 250),
        VenueCapacity("Rimba, TARUMT (400 seats)", 400, 80, 320),
        VenueCapacity("DTAR, TARUMT (700 seats)", 700, 200, 500),
        VenueCapacity("Sport Complex, TARUMT (1000 seats)", 1000, 300, 700),
        VenueCapacity("Arena, TARUMT (1500 seats)", 1000, 500, 1000)
    )

    val locationOptions = venueCapacities.map { it.name }

    val categoryOptions = listOf("Academic", "Career", "Art", "Wellness", "Music", "Festival")

    // Dynamic title and button text based on mode
    val screenTitle = if (isEditMode) "Edit Event" else "Create Event"
    val submitButtonText = if (isEditMode) "Save" else "Create"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontSize = 18.sp) },  // Dynamic title
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Event Profile Picture
            // In Edit mode: shows existing image, In Create mode: shows placeholder
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isEditMode) 140.dp else 160.dp)
                        .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .clickable{
                            imagePicker.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Show newly selected image first, then existing image, then placeholder
                    when {
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Event Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        !initialEventData?.imagePath.isNullOrBlank() -> {
                            AsyncImage(
                                model = File(initialEventData?.imagePath!!),
                                contentDescription = "Event Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
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
//                    // !!!initialEventData != null -> eventName.isNotEmpty()!!!
//                    if (isEditMode && initialEventData != null) {
//                        // EDIT MODE: Show existing event image
//                        val context = LocalContext.current
//                        val imageRes = remember(initialEventData.imagePath) {
//                            if (initialEventData.imagePath != null && initialEventData.imagePath.isNotBlank()) {
//                                context.resources.getIdentifier(
//                                    initialEventData.imagePath,
//                                    "drawable",
//                                    context.packageName
//                                )
//                            } else {
//                                0
//                            }
//                        }
//
//                        if (imageRes != 0) {
//                            Image(
//                                painter = painterResource(id = imageRes),
//                                contentDescription = eventName,
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
//                        } else {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp)),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = eventName,
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color.White,
//                                    textAlign = TextAlign.Center
//                                )
//                            }
//                        }
//                    } else {
//                        // CREATE MODE: Show upload placeholder
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Image,
//                                contentDescription = "Upload Image",
//                                modifier = Modifier.size(48.dp),
//                                tint = Color.Gray
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                // !!!TAP TO UPLOAD IMAGE??!!!
//                                text = "Event profile picture",
//                                fontSize = 14.sp,
//                                color = Color.Gray
//                            )
//                        }
//                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Event Name Field
            item {
                FormFieldLabel("Event name", isRequired = true)
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    placeholder = {
                        // Only show placeholder in Create mode
                        if (!isEditMode) Text("Enter event name", fontSize = 14.sp, color = Color.Gray)
                    },
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
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            placeholder = { if (!isEditMode) Text("Enter date", fontSize = 14.sp, color = Color.Gray) },
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
                        FormFieldLabel("Duration", isRequired = true)
                        OutlinedTextField(
                            value = eventDuration,
                            onValueChange = { eventDuration = it },
                            placeholder = { if (!isEditMode) Text("Enter duration", fontSize = 14.sp, color = Color.Gray) },
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

            // Time Field
            item {
                FormFieldLabel("Time", isRequired = true)
                OutlinedTextField(
                    value = eventTime,
                    onValueChange = { eventTime = it },
                    placeholder = { if (!isEditMode) Text("Enter time", fontSize = 14.sp, color = Color.Gray) },
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
                        placeholder = { if (!isEditMode) Text("Select a category", fontSize = 14.sp, color = Color.Gray) },
                        trailingIcon = {
                            Icon(
                                imageVector = if (categoryExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
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
                        placeholder = { if (!isEditMode) Text("Select a location", fontSize = 14.sp, color = Color.Gray) },
                        trailingIcon = {
                            Icon(
                                imageVector = if (locationExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
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

                                    // Autofill rock and normal zone seats
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

            // Rock Zone and Normal Zone Seats
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
                            placeholder = { if (!isEditMode) Text("Seats quantity", fontSize = 14.sp, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color(0xFFE0E0E0),
                                disabledContainerColor = Color(0xFFF5F5F5)
                            ),
                            singleLine = true,
                            readOnly = true // autofill - cannot fill in
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Normal zone seats", isRequired = true)
                        OutlinedTextField(
                            value = normalZoneSeats,
                            onValueChange = { normalZoneSeats = it },
                            placeholder = { if (!isEditMode) Text("Seats quantity", fontSize = 14.sp, color = Color.Gray) },
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

            // Rock Zone and Normal Zone Price
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
//                                val cleaned = input.replace("[^0-9.]".toRegex(), "")
//
//                                // Match numbers with optional decimal part (max 2 decimals)
//                                if (cleaned.matches(Regex("^\\d+(\\.\\d{0,2})?$"))) {
//                                    rockZonePrice = cleaned
//                                }
                                if (input.isEmpty()) {
                                    rockZonePrice = ""
                                } else {
                                    // !!!Remove non-numeric characters except decimal point!!!
                                    val cleaned = input.filter { it.isDigit() || it == '.' }

                                    // !!!Validate format: digits with optional decimal (max 2 decimals)!!!
                                    // !!!Allow partial inputs like "5." for better UX!!!
                                    if (cleaned.matches(Regex("^\\d+\\.?\\d{0,2}$"))) {
                                        rockZonePrice = cleaned
                                    }
                                }
                            },
                            placeholder = {
                                if (!isEditMode)
                                    Text("0.00", fontSize = 14.sp, color = Color.Gray)
                            },
                            leadingIcon = {
                                Text("RM", fontSize = 14.sp, color = Color.Black)
                            },
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
//                                val cleaned = input.replace("[^0-9.]".toRegex(), "")
//
//                                // Match numbers with optional decimal part (max 2 decimals)
//                                if (cleaned.matches(Regex("^\\d+(\\.\\d{0,2})?$"))) {
//                                    normalZonePrice = cleaned
//                                }
                                if (input.isEmpty()) {
                                    normalZonePrice = ""
                                } else {
                                    // !!!Remove non-numeric characters except decimal point!!!
                                    val cleaned = input.filter { it.isDigit() || it == '.' }

                                    // !!!Validate format: digits with optional decimal (max 2 decimals)!!!
                                    // !!!Allow partial inputs like "5." for better UX!!!
                                    if (cleaned.matches(Regex("^\\d+\\.?\\d{0,2}$"))) {
                                        normalZonePrice = cleaned
                                    }
                                }
                            },
                            placeholder = {
                                if (!isEditMode)
                                    Text("0.00", fontSize = 14.sp, color = Color.Gray)
                            },
                            leadingIcon = {
                                Text("RM", fontSize = 14.sp, color = Color.Black)
                            },
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
                    placeholder = { if (!isEditMode) Text("Enter description", fontSize = 14.sp, color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF2196F3)
                    ),
                    maxLines = 5
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Submit Button (Create or Save based on mode)
            item {
                var showError by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf("") }

                // !!!Show error message if validation fails!!!
                if (showError) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                fontSize = 13.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                // Validation
                Button(
                    onClick = {
                        // Validate all required fields before submission!!!
                        when {
                            eventName.isBlank() -> {
                                showError = true
                                errorMessage = "Please enter event name"
                            }
                            eventDate.isBlank() -> {
                                showError = true
                                errorMessage = "Please enter event date"
                            }
                            eventDuration.isBlank() -> {
                                showError = true
                                errorMessage = "Please enter event duration"
                            }
                            eventTime.isBlank() -> {
                                showError = true
                                errorMessage = "Please enter event time"
                            }
                            selectedCategory.isBlank() -> {
                                showError = true
                                errorMessage = "Please select a category"
                            }
                            selectedLocation.isBlank() -> {
                                showError = true
                                errorMessage = "Please select a location"
                            }
                            rockZonePrice.isBlank() || rockZonePrice.toDoubleOrNull() == null -> {
                                showError = true
                                errorMessage = "Please enter valid rock zone price"
                            }
                            normalZonePrice.isBlank() || normalZonePrice.toDoubleOrNull() == null -> {
                                showError = true
                                errorMessage = "Please enter valid normal zone price"
                            }
                            eventDescription.isBlank() -> {
                                showError = true
                                errorMessage = "Please enter event description"
                            }
                            else -> {
                                // !!!All validations passed, submit the form!!!
                                showError = false
                                val formData = EventFormData(
                                    name = eventName,
                                    date = eventDate,
                                    duration = eventDuration,
                                    time = eventTime,
                                    location = selectedLocation,
                                    category = selectedCategory,
                                    rockZoneSeats = rockZoneSeats,
                                    normalZoneSeats = normalZoneSeats,
                                    rockZonePrice = rockZonePrice.toDoubleOrNull() ?: 0.0,
                                    normalZonePrice = normalZonePrice.toDoubleOrNull() ?: 0.0,
                                    description = eventDescription,
                                    imagePath = null)
                                onSubmitClick(formData)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = submitButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}


// Helper composable for consistent field labels
@Composable
private fun FormFieldLabel(text: String, isRequired: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        // Show red asterisk (*) for compulsory field
        if (isRequired) {
            Text(
                text = " *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// ==================== WRAPPER SCREENS ====================

@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit = {},
    onCreateClick: (EventFormData) -> Unit = {},
    onImageSelected: ((android.net.Uri) -> Unit)? = null
) {
    EventFormScreen(
        isEditMode = false,           // CREATE MODE
        initialEventData = null,      // No initial data - empty form
        onBackClick = onBackClick,
        onSubmitClick = onCreateClick,
        onImageSelected = onImageSelected
    )
}

@Composable
fun EditEventScreen(
    eventId: String,
    onBackClick: () -> Unit = {},
    onSaveClick: (EventFormData) -> Unit = {},
    onImageSelected: ((android.net.Uri) -> Unit)? = null
) {
    // TODO: Load event data from ViewModel using eventId
    // For now, using sample data
    val existingEventData = EventFormData(
        name = "MUSIC FIESTA 6.0",
        date = "12/01/2026",
        duration = "3 hour",
        time = "19.00 PM",
        location = "Rimba, TARUMT (400 seats)",
        category = "Music",
        rockZoneSeats = "80",
        normalZoneSeats = "320",
        rockZonePrice = 60.00,
        normalZonePrice = 35.00,
        description = "Music fiesta 6.0 is a large-scale campus concert and carnival proudly organized by Music Society of Tunku Abdul Rahman University of Management and Technology (TARUMT).",
        imagePath = null
//        imagePath = "musicfiesta" // Sample drawable name for preview
    )

    EventFormScreen(
        isEditMode = true,                      // EDIT MODE
        initialEventData = existingEventData,   // Pre-filled with existing data
        onBackClick = onBackClick,
        onSubmitClick = onSaveClick,
        onImageSelected = onImageSelected
    )
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