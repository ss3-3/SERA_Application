package com.example.sera_application.presentation.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.domain.model.User
import com.example.sera_application.presentation.viewmodel.user.UserListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    onBack: () -> Unit,
    viewModel: UserListViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("User Management", color = Color.White) }, // Updated title
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2C2C2E)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
//            } else {
//                LazyColumn(
//                    contentPadding = PaddingValues(16.dp)
//                ) {
//                    items(users) { user ->
//                        UserItem(user = user) // Reusing UserItem via copying or shared?
//                        // Since I can't easily share composables without a common file, I'll inline or duplicate for now to respect "using AdminUserManagementScreen.kt"
//                        // I'll assume UserItem can be copied here for isolation.
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
            }
        }
    }
}

//@Composable
//fun UserItem(user: User) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = Icons.Default.Person,
//                    contentDescription = "User",
//                    tint = MaterialTheme.colorScheme.primary
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = user.fullName,
//                    style = MaterialTheme.typography.titleMedium
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = Icons.Default.Email,
//                    contentDescription = "Email",
//                    tint = MaterialTheme.colorScheme.secondary
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = user.email,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
//
//            if (user.phone != null) {
//                Spacer(modifier = Modifier.height(4.dp))
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Default.Phone,
//                        contentDescription = "Phone",
//                        tint = MaterialTheme.colorScheme.secondary
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = user.phone,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Role: ${user.role.name}",
//                style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//}
