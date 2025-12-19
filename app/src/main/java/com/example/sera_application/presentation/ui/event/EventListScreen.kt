package com.example.sera_application.presentation.ui.event

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.domain.model.Event
import com.example.sera_application.presentation.viewmodel.event.EventListViewModel

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EventListScreen(
//    onBack: () -> Unit,
//    onEventClick: (String) -> Unit, // Add callback
//    viewModel: EventListViewModel = hiltViewModel()
//) {
//    val events by viewModel.events.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val error by viewModel.error.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Events") }
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            } else if (error != null) {
//                Text(
//                    text = error ?: "Unknown error",
//                    color = MaterialTheme.colorScheme.error,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            } else {
//                LazyColumn(
//                    contentPadding = PaddingValues(16.dp)
//                ) {
//                    items(events) { event ->
//                        EventItem(
//                            event = event,
//                            onClick = { onEventClick(event.id) }
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun EventItem(event: Event, onClick: () -> Unit) { // Add onClick
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        onClick = onClick // Use Card onClick (Material3)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Text(
//                text = event.name,
//                style = MaterialTheme.typography.titleMedium
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "${event.date} at ${event.startTime} - ${event.endTime}",
//                style = MaterialTheme.typography.bodyMedium
//            )
//            Text(
//                text = event.location,
//                style = MaterialTheme.typography.bodySmall
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = event.priceRange,
//                style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//}
