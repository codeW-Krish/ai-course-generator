package com.example.jetpackdemo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.jetpackdemo.data.model.DefaultProvidersResponse
import com.example.jetpackdemo.shared_pref.UserPreferencesManager
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.utils.TokenManager
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.AdminViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavHostController,
    courseViewModel: CourseViewModel,
    adminViewModel: AdminViewModel? = null
) {
    val context = LocalContext.current
    val userPrefsManager = remember { UserPreferencesManager(context) }
    val tokenManager = remember { TokenManager(context) }

    // === ViewModel State ===
    val userRole by courseViewModel.userRole.collectAsState()
    val availableProviders by courseViewModel.availableProviders.collectAsState()
    val defaultProviders by courseViewModel.defaultProviders.collectAsState()
    val outlineProvider by courseViewModel.selectedOutlineProvider.collectAsState()
    val contentProvider by courseViewModel.selectedContentProvider.collectAsState()

    // === Local UI State ===
    var showSuccessToast by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // === Use only real providers (no fallback) ===
    val providers = availableProviders

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = AppColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileHeader()
                Spacer(modifier = Modifier.height(32.dp))

                // === ADMIN SECTION ===
                if (userRole == "admin" && adminViewModel != null) {
                    SafeAdminSettingsSection(
                        adminViewModel = adminViewModel,
                        availableProviders = availableProviders,
                        defaultProviders = defaultProviders,
                        onViewAllCourses = { navController.navigate("adminCourses") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // === PROVIDER SELECTION ===
                ProviderSelectionSection(
                    outlineProvider = outlineProvider,
                    contentProvider = contentProvider,
                    availableProviders = providers,
                    onOutlineProviderChange = { newProvider ->
                        courseViewModel.updateProviders(contentProvider, newProvider)
                    },
                    onContentProviderChange = { newProvider ->
                        courseViewModel.updateProviders(newProvider, outlineProvider)
                    },
                    onSaveClick = { showSuccessToast = true }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // === LOGOUT BUTTON ===
                Button(
                    onClick = {
                        userPrefsManager.clearAll()
                        courseViewModel.clearUserData()
                        tokenManager.clearTokens()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
//                        navController.navigate("login") {
//                            popUpTo("main") { inclusive = false }
//                            launchSingleTop = true
//                        }
//                        navController.navigate("login") {
//                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
//                            launchSingleTop = true
//                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Log Out", color = Color.White)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // === SUCCESS TOAST (Auto-dismiss after 2s) ===
            if (showSuccessToast) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showSuccessToast = false
                }
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF10A37F),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Settings saved successfully!")
                }
            }
        }
    }
}

// === ADMIN SECTION ===
@Composable
fun SafeAdminSettingsSection(
    adminViewModel: AdminViewModel,
    availableProviders: List<String>,
    defaultProviders: DefaultProvidersResponse?,
    onViewAllCourses: () -> Unit
) {
    AdminSettingsSection(
        adminViewModel = adminViewModel,
        availableProviders = availableProviders,
        defaultProviders = defaultProviders,
        onViewAllCourses = onViewAllCourses
    )
}

@Composable
fun AdminSettingsSection(
    adminViewModel: AdminViewModel,
    availableProviders: List<String>,
    defaultProviders: DefaultProvidersResponse?,
    onViewAllCourses: () -> Unit
) {
    var showProviderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        adminViewModel.loadAvailableProviders()
        adminViewModel.loadDefaultProviders()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D3E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Admin Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "ADMIN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10A37F),
                    modifier = Modifier
                        .background(Color(0xFF10A37F).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Manage application-wide settings and content", fontSize = 14.sp, color = Color(0xFFAAAAAA), lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Available Providers", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text("Current: ${availableProviders.joinToString(", ")}", fontSize = 14.sp, color = Color(0xFFAAAAAA))
            Spacer(modifier = Modifier.height(12.dp))

            Text("Default Providers", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            defaultProviders?.let { prov ->
                Text("Outline: ${prov.outline}, Content: ${prov.content}", fontSize = 14.sp, color = Color(0xFFAAAAAA))
            } ?: Text("Loading...", fontSize = 14.sp, color = Color(0xFFAAAAAA))

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showProviderDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10A37F))
                ) { Text("Manage Providers") }

                Button(
                    onClick = onViewAllCourses,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                ) { Text("Manage Courses") }
            }
        }
    }

    if (showProviderDialog) {
        ProviderManagementDialog(
            availableProviders = availableProviders,
            defaultProviders = defaultProviders,
            onDismiss = { showProviderDialog = false },
            onUpdateAvailableProviders = { adminViewModel.updateAvailableProviders(it) },
            onUpdateDefaultProviders = { outline, content -> adminViewModel.updateDefaultProviders(outline, content) }
        )
    }
}

// === PROVIDER SELECTION (All Users) ===
@Composable
fun ProviderSelectionSection(
    outlineProvider: String,
    contentProvider: String,
    availableProviders: List<String>,
    onOutlineProviderChange: (String) -> Unit,
    onContentProviderChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Provider Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Choose which AI provider to use for generating course content", fontSize = 14.sp, color = AppColors.textSecondary, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Outline Generation", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
            DropdownMenuProvider(
                selectedProvider = outlineProvider,
                onProviderSelected = onOutlineProviderChange,
                providers = availableProviders,
                label = "Select provider for course outlines"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Content Generation", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
            DropdownMenuProvider(
                selectedProvider = contentProvider,
                onProviderSelected = onContentProviderChange,
                providers = availableProviders,
                label = "Select provider for course content"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) { Text("Save Settings", color = AppColors.onPrimary) }
        }
    }
}

// === PROFILE HEADER ===
@Composable
fun ProfileHeader() {
    val context = LocalContext.current
    val userPrefsManager = remember { UserPreferencesManager(context) }
    // Read saved username and email directly from SharedPreferences
    val username = remember { userPrefsManager.getUsername() ?: "User" }
    val email = remember { userPrefsManager.getEmail() ?: "user@example.com" }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Gray, shape = RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(username, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text(email, fontSize = 14.sp, color = AppColors.textSecondary)
    }
}

// === DROPDOWN PROVIDER ===
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DropdownMenuProvider(
//    selectedProvider: String,
//    onProviderSelected: (String) -> Unit,
//    providers: List<String>,
//    label: String
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded }
//    ) {
//        OutlinedTextField(
//            value = selectedProvider,
//            onValueChange = {},
//            readOnly = true,
//            label = { Text(label) },
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//            modifier = Modifier
//                .menuAnchor()
//                .fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = AppColors.primary,
//                unfocusedBorderColor = AppColors.textSecondary.copy(alpha = 0.4f),
//                focusedLabelColor = AppColors.primary,
//                cursorColor = AppColors.primary,
//                containerColor = AppColors.surface
//            )
//        )
//
//        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//            providers.forEach { provider ->
//                DropdownMenuItem(
//                    text = { Text(provider) },
//                    onClick = {
//                        onProviderSelected(provider)
//                        expanded = false
//                    }
//                )
//            }
//        }
//    }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuProvider(
    selectedProvider: String,
    onProviderSelected: (String) -> Unit,
    providers: List<String>,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    // ADD THIS LINE – FORCE VALID SELECTION
    val displayProvider = if (selectedProvider in providers) selectedProvider else providers.firstOrNull() ?: "None"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayProvider,  // Use safe value
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.primary,
                unfocusedBorderColor = AppColors.textSecondary.copy(alpha = 0.4f),
                focusedLabelColor = AppColors.primary,
                cursorColor = AppColors.primary,
                focusedContainerColor = AppColors.surface,
                unfocusedContainerColor = AppColors.surface
            )
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            providers.forEach { provider ->
                DropdownMenuItem(
                    text = { Text(provider) },
                    onClick = {
                        onProviderSelected(provider)
                        expanded = false
                    }
                )
            }
        }
    }
}


// === PROVIDER MANAGEMENT DIALOG ===
@Composable
fun ProviderManagementDialog(
    availableProviders: List<String>,
    defaultProviders: DefaultProvidersResponse?,
    onDismiss: () -> Unit,
    onUpdateAvailableProviders: (List<String>) -> Unit,
    onUpdateDefaultProviders: (String, String) -> Unit
) {
    var currentProviders by remember { mutableStateOf(availableProviders.toMutableList()) }
    var selectedOutline by remember { mutableStateOf(defaultProviders?.outline ?: "Groq") }
    var selectedContent by remember { mutableStateOf(defaultProviders?.content ?: "Groq") }
    var newProvider by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage AI Providers") },
        text = {
            Column {
                Text("Available Providers", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                currentProviders.forEach { provider ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(provider)
                        IconButton(onClick = { currentProviders.remove(provider) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newProvider,
                        onValueChange = { newProvider = it },
                        label = { Text("New Provider") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newProvider.isNotBlank() && newProvider !in currentProviders) {
                            currentProviders.add(newProvider)
                            newProvider = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Default Providers", fontWeight = FontWeight.Bold)

                DropdownMenuProvider(
                    selectedProvider = selectedOutline,
                    onProviderSelected = { selectedOutline = it },
                    providers = currentProviders,
                    label = "Default Outline Provider"
                )
                Spacer(modifier = Modifier.height(8.dp))
                DropdownMenuProvider(
                    selectedProvider = selectedContent,
                    onProviderSelected = { selectedContent = it },
                    providers = currentProviders,
                    label = "Default Content Provider"
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Validate defaults are in available
                if (selectedOutline !in currentProviders) {
                    // Fallback to first available
                    selectedOutline = currentProviders.firstOrNull() ?: "Groq"
                }
                if (selectedContent !in currentProviders) {
                    selectedContent = currentProviders.firstOrNull() ?: "Groq"
                }
                onUpdateAvailableProviders(currentProviders)
                onUpdateDefaultProviders(selectedOutline, selectedContent)
                onDismiss()
            }) {
                Text("Save All")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}