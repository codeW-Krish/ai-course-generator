package com.example.jetpackdemo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetpackdemo.data.model.DefaultProvidersResponse
import com.example.jetpackdemo.data.model.UpdateProfileRequest
import com.example.jetpackdemo.shared_pref.UserPreferencesManager
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.utils.TokenManager
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.AdminViewModel
import com.example.jetpackdemo.viewmodels.Resource
import com.example.jetpackdemo.viewmodels.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavHostController,
    appNavController: NavHostController,
    courseViewModel: CourseViewModel,
    adminViewModel: AdminViewModel? = null,
    userViewModel: UserViewModel? = null
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

    // === User Profile State ===
    val myProfile = userViewModel?.myProfile?.collectAsStateWithLifecycle()
    val profileData = myProfile?.value

    // === Local UI State ===
    var showSuccessToast by remember { mutableStateOf(false) }
    var showEditBioDialog by remember { mutableStateOf(false) }
    var showEditProfilePicDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // === Use only real providers (no fallback) ===
    val providers = availableProviders

    // Load profile on mount
    LaunchedEffect(Unit) {
        userViewModel?.loadMyProfile()
    }

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

                // === PROFILE HEADER with picture, stats ===
                ProfileHeaderEnhanced(
                    profileData = profileData,
                    userPrefsManager = userPrefsManager,
                    onEditPicture = { showEditProfilePicDialog = true },
                    onEditBio = { showEditBioDialog = true },
                    onFollowersClick = {
                        val userId = (profileData as? Resource.Success)?.data?.id
                        if (userId != null) navController.navigate("followers_list/$userId")
                    },
                    onFollowingClick = {
                        val userId = (profileData as? Resource.Success)?.data?.id
                        if (userId != null) navController.navigate("following_list/$userId")
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                        appNavController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
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

    // === Edit Bio Dialog ===
    if (showEditBioDialog && userViewModel != null) {
        var bioText by remember {
            mutableStateOf((profileData as? Resource.Success)?.data?.bio ?: "")
        }
        AlertDialog(
            onDismissRequest = { showEditBioDialog = false },
            title = { Text("Edit Bio") },
            text = {
                OutlinedTextField(
                    value = bioText,
                    onValueChange = { bioText = it },
                    label = { Text("Your bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    userViewModel.updateMyProfile(UpdateProfileRequest(bio = bioText))
                    showEditBioDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditBioDialog = false }) { Text("Cancel") }
            }
        )
    }

    // === Edit Profile Picture Dialog ===
    if (showEditProfilePicDialog && userViewModel != null) {
        var imageUrl by remember {
            mutableStateOf((profileData as? Resource.Success)?.data?.profileImageUrl ?: "")
        }
        AlertDialog(
            onDismissRequest = { showEditProfilePicDialog = false },
            title = { Text("Profile Picture") },
            text = {
                Column {
                    Text("Enter an image URL for your profile picture:", fontSize = 14.sp, color = AppColors.textSecondary)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (imageUrl.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Preview:", fontSize = 13.sp, color = AppColors.textSecondary)
                        Spacer(Modifier.height(8.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    userViewModel.updateMyProfile(UpdateProfileRequest(profileImageUrl = imageUrl))
                    showEditProfilePicDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfilePicDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// === ENHANCED PROFILE HEADER ===
@Composable
fun ProfileHeaderEnhanced(
    profileData: Resource<com.example.jetpackdemo.data.model.UserProfile>?,
    userPrefsManager: UserPreferencesManager,
    onEditPicture: () -> Unit,
    onEditBio: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    val username = remember { userPrefsManager.getUsername() ?: "User" }
    val email = remember { userPrefsManager.getEmail() ?: "user@example.com" }

    val profile = (profileData as? Resource.Success)?.data

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Profile Picture with edit overlay
        Box(contentAlignment = Alignment.BottomEnd) {
            if (profile?.profileImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.profileImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onEditPicture),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AppColors.primary.copy(alpha = 0.15f))
                        .clickable(onClick = onEditPicture),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        username.first().uppercase(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.primary
                    )
                }
            }
            // Edit icon
            Surface(
                shape = CircleShape,
                color = AppColors.primary,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onEditPicture)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = AppColors.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(profile?.username ?: username, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text(profile?.email ?: email, fontSize = 14.sp, color = AppColors.textSecondary)

        // Bio
        if (profile?.bio != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                profile.bio,
                fontSize = 14.sp,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        TextButton(onClick = onEditBio) {
            Text(
                if (profile?.bio.isNullOrBlank()) "Add a bio" else "Edit bio",
                fontSize = 13.sp,
                color = AppColors.accent
            )
        }

        Spacer(Modifier.height(12.dp))

        // Social stats row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    count = profile?.coursesCount ?: 0,
                    label = "Courses",
                    onClick = {}
                )
                Box(Modifier.width(1.dp).height(40.dp).background(AppColors.textSecondary.copy(alpha = 0.2f)))
                StatItem(
                    count = profile?.followersCount ?: 0,
                    label = "Learners",
                    onClick = onFollowersClick
                )
                Box(Modifier.width(1.dp).height(40.dp).background(AppColors.textSecondary.copy(alpha = 0.2f)))
                StatItem(
                    count = profile?.followingCount ?: 0,
                    label = "Mentors",
                    onClick = onFollowingClick
                )
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            "$count",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
        Text(label, fontSize = 13.sp, color = AppColors.textSecondary)
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