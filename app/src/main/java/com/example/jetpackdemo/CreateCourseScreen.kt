package com.example.jetpackdemo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel

// ─────────────────────────────────────────────────────────────────────────────
// 1. REUSABLE PROVIDER DROPDOWN
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDropdown(
    label: String,
    selectedProvider: String,
    providers: List<String>,
    onProviderSelected: (String) -> Unit
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
            shape = RoundedCornerShape(12.dp),
            colors = getTextFieldColors()
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

// ─────────────────────────────────────────────────────────────────────────────
// 2. MAIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    courseViewModel: CourseViewModel,
    onNavigateBack: () -> Unit,
    onGenerateOutline: () -> Unit
) {


    // ────── UI STATE ──────
    var courseTitle by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var includeYouTube by remember { mutableStateOf(false) }
    var numberOfUnits by remember { mutableStateOf(0) }
    var difficultyLevel by remember { mutableStateOf("") }
    var isInteractiveMode by remember { mutableStateOf(false) } // Normal vs Interactive

    // ────── VIEWMODEL STATE ──────
    val contentProvider by courseViewModel.selectedContentProvider.collectAsState()
    val outlineProvider by courseViewModel.selectedOutlineProvider.collectAsState()
    val availableProviders by courseViewModel.availableProviders.collectAsState()

    // Local UI copies – they are kept in sync with the ViewModel
    var selectedContentProvider by remember { mutableStateOf(contentProvider) }
    var selectedOutlineProvider by remember { mutableStateOf(outlineProvider) }

    // Keep UI in sync when ViewModel changes (e.g. after clearUserData)
    LaunchedEffect(contentProvider) { selectedContentProvider = contentProvider }
    LaunchedEffect(outlineProvider) { selectedOutlineProvider = outlineProvider }

    // ────── MAX UNITS LOGIC (now respects BOTH providers) ──────
    val maxUnits = remember(selectedContentProvider) {
        when (selectedContentProvider.uppercase()) {
            "CEREBRAS" -> 10
            "GROQ"     -> 6
            "GEMINI"   -> 4
            else       -> 6
        }
    }

    val unitItems = remember(maxUnits) {
        (1..maxUnits).map { if (it == 1) "1 Unit" else "$it Units" }
    }

    // ────── FORM VALIDATION ──────
    val isFormValid = courseTitle.isNotBlank() &&
            courseDescription.isNotBlank() &&
            numberOfUnits in 1..maxUnits &&
            difficultyLevel.isNotBlank()

    // ────── UI ──────
    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Course",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* more options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = AppColors.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            // 1. Persist the selected providers
                            courseViewModel.updateProviders(
                                contentProvider = selectedContentProvider,
                                outlineProvider = selectedOutlineProvider
                            )

                            // 2. Set the learning mode (normal vs interactive)
                            courseViewModel.setIsInteractiveMode(isInteractiveMode)

                            // 3. Prepare the request (outline provider is used for outline generation)
                            courseViewModel.prepareOutlineRequest(
                                title = courseTitle,
                                description = courseDescription,
                                numUnits = numberOfUnits,
                                difficulty = difficultyLevel,
                                includeVideos = includeYouTube,
                                provider = selectedOutlineProvider,
                                model = null
                            )
                            onGenerateOutline()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                        enabled = isFormValid
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Generate Outline",
                            tint = AppColors.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Generate Outline",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This will create a personalized course outline based on your preferences",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Course Title ──
            FormSection(label = "Course Title", hint = "Give your course a clear and descriptive title") {
                OutlinedTextField(
                    value = courseTitle,
                    onValueChange = { courseTitle = it },
                    placeholder = { Text("Enter your course title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors()
                )
            }

            // ── Brief Description ──
            FormSection(label = "Brief Description", hint = "Provide a brief overview of the course content and objectives") {
                OutlinedTextField(
                    value = courseDescription,
                    onValueChange = { courseDescription = it },
                    placeholder = { Text("Describe what your course will cover...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors()
                )
            }

            // ── Number of Units ──
            FormSection(label = "Number of Units", hint = "Choose how many units your course should have") {
                DropdownSelector(
                    label = "Select unit count",
                    items = unitItems,
                    selectedText = if (numberOfUnits in 1..maxUnits) {
                        if (numberOfUnits == 1) "1 Unit" else "$numberOfUnits Units"
                    } else "Select unit count",
                    onItemSelected = { numberOfUnits = it.trim().split(" ").first().toInt() }
                )
            }

            // ── Difficulty Level ──
            FormSection(label = "Difficulty Level", hint = "Set the appropriate difficulty level for your target audience") {
                DropdownSelector(
                    label = "Select difficulty",
                    items = listOf("Beginner", "Intermediate", "Advanced"),
                    selectedText = if (difficultyLevel.isBlank()) "Select difficulty" else difficultyLevel,
                    onItemSelected = { difficultyLevel = it }
                )
            }

            // ── Include YouTube Videos ──
            FormSection(label = "Include YouTube Videos?", hint = "Add relevant video content to enhance learning") {
                Switch(
                    checked = includeYouTube,
                    onCheckedChange = { includeYouTube = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.primary,
                        checkedTrackColor = AppColors.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = AppColors.textSecondary,
                        uncheckedTrackColor = AppColors.textSecondary.copy(alpha = 0.2f)
                    )
                )
            }

            // ── Learning Mode Selection ──
            FormSection(
                label = "Interactive Learning Mode",
                hint = if (isInteractiveMode) "Quiz-based learning with hearts & hints" else "Traditional content generation"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isInteractiveMode) "Interactive (Quiz Mode)" else "Normal (Read Mode)",
                        color = if (isInteractiveMode) AppColors.primary else AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isInteractiveMode,
                        onCheckedChange = { isInteractiveMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.primary,
                            checkedTrackColor = AppColors.primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = AppColors.textSecondary,
                            uncheckedTrackColor = AppColors.textSecondary.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            // ── PROVIDER SELECTION SECTION ──
            Spacer(modifier = Modifier.height(24.dp))
            Text("AI Providers", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            // Outline Provider
            FormSection(label = "Outline Generation Provider", hint = "Select the model that will create the course outline") {
                ProviderDropdown(
                    label = "Outline Provider",
                    selectedProvider = selectedOutlineProvider,
                    providers = availableProviders,
                    onProviderSelected = { newProvider ->
                        selectedOutlineProvider = newProvider
                        // Persist immediately – optional, but nice UX
                        courseViewModel.updateProviders(
                            contentProvider = selectedContentProvider,
                            outlineProvider = newProvider
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content Provider
            FormSection(label = "Content Generation Provider", hint = "Select the model that will generate the unit content") {
                ProviderDropdown(
                    label = "Content Provider",
                    selectedProvider = selectedContentProvider,
                    providers = availableProviders,
                    onProviderSelected = { newProvider ->
                        selectedContentProvider = newProvider
                        courseViewModel.updateProviders(
                            contentProvider = newProvider,
                            outlineProvider = selectedOutlineProvider
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. RE-USABLE FORM SECTION & DROPDOWN (unchanged except for minor styling)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FormSection(label: String, hint: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Spacer(modifier = Modifier.height(4.dp))
        Text(hint, fontSize = 12.sp, color = AppColors.textSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    items: List<String>,
    selectedText: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = getTextFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun getTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedIndicatorColor = AppColors.primary,
    unfocusedIndicatorColor = AppColors.textSecondary.copy(alpha = 0.4f),
    focusedContainerColor = AppColors.surface,
    unfocusedContainerColor = AppColors.surface,
    cursorColor = AppColors.primary,
    focusedLabelColor = AppColors.primary
)