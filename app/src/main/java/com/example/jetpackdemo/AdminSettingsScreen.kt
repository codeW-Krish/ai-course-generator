package com.example.jetpackdemo


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.jetpackdemo.data.api.GenericResponse
import com.example.jetpackdemo.data.model.AvailableProvidersResponse
import com.example.jetpackdemo.data.model.DefaultProvidersResponse
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.ui.viewmodel.Resource
import com.example.jetpackdemo.viewmodels.AdminViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel
) {
    // Double-check: non-admins should never reach here
    if (!adminViewModel.isAdmin) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val availableProviders by adminViewModel.availableProviders.collectAsState()
    val defaultProviders by adminViewModel.defaultProviders.collectAsState()
    val updateStatus by adminViewModel.updateStatus.collectAsState()

    // Load data on first appear
    LaunchedEffect(Unit) {
        adminViewModel.loadAvailableProviders()
        adminViewModel.loadDefaultProviders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Settings", color = AppColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Global Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // === Provider Management Card ===
            ProviderManagementCard(
                availableProviders = availableProviders,
                defaultProviders = defaultProviders,
                updateStatus = updateStatus,
                onManageProviders = {
                    // Optional: open dialog or navigate
                    // For now, just reload
                    adminViewModel.loadAvailableProviders()
                    adminViewModel.loadDefaultProviders()
                },
                onClearStatus = { adminViewModel.clearUpdateStatus() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // === Future Admin Settings Go Here ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "More Settings Coming Soon",
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• User management\n• Course analytics\n• System logs",
                        color = AppColors.textSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderManagementCard(
    availableProviders: Resource<AvailableProvidersResponse>?,
    defaultProviders: Resource<DefaultProvidersResponse>?,
    updateStatus: Resource<GenericResponse>?,
    onManageProviders: () -> Unit,
    onClearStatus: () -> Unit
) {
    var showStatus by remember { mutableStateOf(updateStatus != null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "AI Provider Management",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Available Providers
            Text("Available Providers", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
            when (availableProviders) {
                is Resource.Loading -> Text("Loading...", color = AppColors.textSecondary)
                is Resource.Success -> {
                    val providers = availableProviders.data!!.providers.joinToString(", ")
                    Text(providers.ifEmpty { "None" }, color = AppColors.textSecondary)
                }
                is Resource.Error -> Text("Error: ${availableProviders.message}", color = Color.Red)
                else -> Text("Not loaded", color = AppColors.textSecondary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Default Providers
            Text("Default Providers", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
            when (defaultProviders) {
                is Resource.Loading -> Text("Loading...", color = AppColors.textSecondary)
                is Resource.Success -> {
                    val def = defaultProviders.data
                    Text("Outline: ${def!!.outline}, Content: ${def.content}", color = AppColors.textSecondary)
                }
                is Resource.Error -> Text("Error: ${defaultProviders.message}", color = Color.Red)
                else -> Text("Not loaded", color = AppColors.textSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onManageProviders,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Refresh Providers", color = AppColors.onPrimary)
            }

            // === Update Status Snackbar ===
            updateStatus?.let { status ->
                if (showStatus) {
                    LaunchedEffect(Unit) {
                        delay(3000)
                        showStatus = false
                        onClearStatus()
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Snackbar(
                        containerColor = when (status) {
                            is Resource.Success -> Color(0xFF10A37F)
                            is Resource.Error -> Color.Red
                            else -> Color.Gray
                        },
                        contentColor = Color.White
                    ) {
                        Text(
                            when (status) {
                                is Resource.Success -> "Updated successfully"
                                is Resource.Error -> "Failed: ${status.message}"
                                else -> "Updating..."
                            }
                        )
                    }
                }
            }
        }
    }
}