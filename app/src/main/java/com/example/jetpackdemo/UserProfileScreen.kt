package com.example.jetpackdemo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetpackdemo.shared_pref.UserPreferencesManager
import com.example.jetpackdemo.ui.theme.AppColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val userPrefsManager = remember { UserPreferencesManager(context) }

    // State for provider selections
    var outlineProvider by rememberSaveable {
        mutableStateOf(userPrefsManager.getOutlineProvider() ?: "Groq")
    }
    var contentProvider by rememberSaveable {
        mutableStateOf(userPrefsManager.getContentProvider() ?: "Groq")
    }
    var showSuccessToast by remember { mutableStateOf(false) }

    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            delay(3000L)
            showSuccessToast = false
        }
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile & Settings",
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary)
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileHeader()
                Spacer(modifier = Modifier.height(32.dp))

                // Provider Selection Section
                ProviderSelectionSection(
                    outlineProvider = outlineProvider,
                    contentProvider = contentProvider,
                    onOutlineProviderChange = { outlineProvider = it },
                    onContentProviderChange = { contentProvider = it },
                    onSaveClick = {
                        userPrefsManager.saveOutlineProvider(outlineProvider)
                        userPrefsManager.saveContentProvider(contentProvider)
                        showSuccessToast = true
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
                AccountActionsSection(
                    onLogoutClick = {
                        userPrefsManager.clearAll()
                        navController.navigate("welcome") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            SuccessToast(
                visible = showSuccessToast,
                message = "Settings saved successfully!",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun ProviderSelectionSection(
    outlineProvider: String,
    contentProvider: String,
    onOutlineProviderChange: (String) -> Unit,
    onContentProviderChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val providers = listOf("Groq", "Cerebras", "Gemini") // Add more as needed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "AI Provider Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Choose which AI provider to use for generating course content",
                fontSize = 14.sp,
                color = AppColors.textSecondary,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Outline Generation Provider
            Text(
                "Outline Generation",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DropdownMenuProvider(
                selectedProvider = outlineProvider,
                onProviderSelected = onOutlineProviderChange,
                providers = providers,
                label = "Select provider for course outlines"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content Generation Provider
            Text(
                "Content Generation",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DropdownMenuProvider(
                selectedProvider = contentProvider,
                onProviderSelected = onContentProviderChange,
                providers = providers,
                label = "Select provider for course content"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Save Settings", color = AppColors.onPrimary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuProvider(
    selectedProvider: String,
    onProviderSelected: (String) -> Unit,
    providers: List<String>,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedProvider,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = AppColors.primary,
                unfocusedIndicatorColor = AppColors.textSecondary.copy(alpha = 0.4f),
                focusedContainerColor = AppColors.surface,
                unfocusedContainerColor = AppColors.surface,
                cursorColor = AppColors.primary,
                focusedLabelColor = AppColors.primary,
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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

@Composable
fun AccountActionsSection(onLogoutClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary.copy(alpha = 0.1f),
                contentColor = AppColors.primary
            )
        ) {
            Text("Log Out")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { /* Handle Delete Account */ }) {
            Text("Delete Account", color = Color.Red.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun SuccessToast(visible: Boolean, message: String, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.progressGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://placehold.co/200x200/3B82F6/FFFFFF?text=K")
                .crossfade(true)
                .build(),
            contentDescription = "User Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Krish", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text("krish.lee@example.com", fontSize = 16.sp, color = AppColors.textSecondary)
    }
}

@Composable
fun ByoApiKeySection(
    apiKey: String,
    isApiKeyVisible: Boolean,
    onApiKeyChange: (String) -> Unit,
    onVisibilityChange: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Your Gemini API Key", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Save your own API key to generate courses without using the app's default credits. Your key is stored securely on this device and is never sent to our servers.",
                fontSize = 14.sp,
                color = AppColors.textSecondary,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter your API Key") },
                singleLine = true,
                visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = onVisibilityChange) {
                        Icon(icon, contentDescription = "Toggle visibility", tint = AppColors.textSecondary)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = AppColors.primary,
                    unfocusedIndicatorColor = AppColors.textSecondary.copy(alpha = 0.4f),
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    cursorColor = AppColors.primary,
                    focusedLabelColor = AppColors.primary,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Save Key", color = AppColors.onPrimary)
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun UserProfileScreenPreview() {
    UserProfileScreen(navController = rememberNavController())
}
