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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(onNavigateBack: () -> Unit, onGenerateOutline: () -> Unit) {
    var courseTitle by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var includeYouTube by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { /* Handle more options */ }) {
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
                        onClick = onGenerateOutline ,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
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

            // Course Title
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

            // Brief Description
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

            // Number of Units
            FormSection(label = "Number of Units", hint = "Choose how many units your course should have") {
                DropdownSelector(
                    label = "Select unit count",
                    items = listOf("3 Units", "4 Units", "5 Units", "6 Units", "7 Units", "8 Units")
                )
            }

            // Difficulty Level
            FormSection(label = "Difficulty Level", hint = "Set the appropriate difficulty level for your target audience") {
                DropdownSelector(
                    label = "Select difficulty",
                    items = listOf("Beginner", "Intermediate", "Advanced", "Expert")
                )
            }

            // Include YouTube Videos
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

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
fun DropdownSelector(label: String, items: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(label) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = getTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        selectedText = item
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun getTextFieldColors(): TextFieldColors {
    // FIXED: Switched to TextFieldDefaults.colors() and used the correct
    // parameter names for modern Material 3 versions. The 'indicatorColor'
    // now controls the border for an OutlinedTextField.
    return TextFieldDefaults.colors(
        focusedIndicatorColor = AppColors.primary,
        unfocusedIndicatorColor = AppColors.textSecondary.copy(alpha = 0.4f),
        focusedContainerColor = AppColors.surface,
        unfocusedContainerColor = AppColors.surface,
        cursorColor = AppColors.primary,
        focusedLabelColor = AppColors.primary,
    )
}


@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun CreateCourseScreenPreview() {
    CreateCourseScreen(onNavigateBack = {}, onGenerateOutline = {})
}
