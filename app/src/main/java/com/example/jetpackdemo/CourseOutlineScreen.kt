package com.example.jetpackdemo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.ui.theme.AppColors

// --- Data Classes with unique IDs for state management ---
data class SubTopic(val id: Int, var title: String)

data class UnitItem(
    val id: Int,
    var title: String,
    val lessonCount: Int,
    val durationMinutes: Int,
    var subTopics: List<SubTopic>
)

// --- Mock Data that can be modified ---
private fun getMockUnits(): List<UnitItem> = listOf(
    UnitItem(1, "Unit 1: Getting Started", 3, 45, listOf(
        SubTopic(101, "What is Machine Learning?"),
        SubTopic(102, "Types of ML Algorithms"),
        SubTopic(103, "Setting Up Your Environment")
    )),
    UnitItem(2, "Unit 2: Data Preprocessing", 4, 60, listOf(
        SubTopic(201, "Data Cleaning"),
        SubTopic(202, "Feature Scaling"),
        SubTopic(203, "Handling Missing Values")
    )),
    UnitItem(3, "Unit 3: Core Algorithms", 5, 90, listOf(
        SubTopic(301, "Linear Regression"),
        SubTopic(302, "Logistic Regression"),
        SubTopic(303, "Decision Trees")
    ))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseOutlineScreen(onNavigateBack: () -> Unit, onGenerateContent: () -> Unit) {
    var units by remember { mutableStateOf(getMockUnits()) }
    var expandedUnitId by remember { mutableStateOf(units.firstOrNull()?.id) }
    var isEditMode by remember { mutableStateOf(false) }
    var tempUnits by remember { mutableStateOf(units) }

    // State for dialogs
    var subTopicToEdit by remember { mutableStateOf<SubTopic?>(null) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }


    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Review Course Outline", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = {
                            units = tempUnits
                            isEditMode = false
                        }) { Icon(Icons.Default.Check, contentDescription = "Save", tint = AppColors.progressGreen) }

                        IconButton(onClick = {
                            tempUnits = units
                            isEditMode = false
                        }) { Icon(Icons.Default.Close, contentDescription = "Cancel", tint = AppColors.textSecondary) }
                    } else {
                        TextButton(onClick = {
                            tempUnits = units.map { it.copy(subTopics = it.subTopics.map { st -> st.copy() }) }
                            isEditMode = true
                        }) {
                            Text("Edit", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
                        }
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
                Button(
                    onClick = onGenerateContent,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Course Content", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.onPrimary)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                CourseHeader()
                Spacer(modifier = Modifier.height(24.dp))
            }
            items(if (isEditMode) tempUnits else units) { unit ->
                UnitCard(
                    unit = unit,
                    isExpanded = expandedUnitId == unit.id,
                    isEditMode = isEditMode,
                    onExpand = { expandedUnitId = if (expandedUnitId == unit.id) null else unit.id },
                    onEditSubTopic = { subTopic -> subTopicToEdit = subTopic },
                    onDeleteSubTopic = { subTopic -> itemToDelete = subTopic },
                    onDeleteUnit = { itemToDelete = unit }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- Dialogs ---
        if (subTopicToEdit != null) {
            EditSubTopicDialog(
                subTopic = subTopicToEdit!!,
                onDismiss = { subTopicToEdit = null },
                onSave = { updatedTitle ->
                    tempUnits = tempUnits.map { unit ->
                        unit.copy(subTopics = unit.subTopics.map {
                            if (it.id == subTopicToEdit!!.id) it.copy(title = updatedTitle) else it
                        })
                    }
                    subTopicToEdit = null
                }
            )
        }

        if (itemToDelete != null) {
            DeleteConfirmationDialog(
                item = itemToDelete!!,
                onDismiss = { itemToDelete = null },
                onConfirm = {
                    when (itemToDelete) {
                        is UnitItem -> {
                            tempUnits = tempUnits.filterNot { it.id == (itemToDelete as UnitItem).id }
                        }
                        is SubTopic -> {
                            tempUnits = tempUnits.map { unit ->
                                unit.copy(subTopics = unit.subTopics.filterNot { it.id == (itemToDelete as SubTopic).id })
                            }
                        }
                    }
                    itemToDelete = null
                }
            )
        }
    }
}

@Composable
fun UnitCard(
    unit: UnitItem,
    isExpanded: Boolean,
    isEditMode: Boolean,
    onExpand: () -> Unit,
    onEditSubTopic: (SubTopic) -> Unit,
    onDeleteSubTopic: (SubTopic) -> Unit,
    onDeleteUnit: () -> Unit
) {
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotation")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.clickable(onClick = onExpand).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(unit.title, fontWeight = FontWeight.Bold, color = AppColors.textPrimary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${unit.lessonCount} lessons • ${unit.durationMinutes} min", color = AppColors.textSecondary, fontSize = 14.sp)
                }
                if (isEditMode) {
                    IconButton(onClick = onDeleteUnit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Unit", tint = AppColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = AppColors.textSecondary,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(color = AppColors.background)
                    Spacer(modifier = Modifier.height(16.dp))
                    unit.subTopics.forEach { subTopic ->
                        SubTopicItem(subTopic, isEditMode, onEdit = { onEditSubTopic(subTopic) }, onDelete = { onDeleteSubTopic(subTopic) })
                    }
                }
            }
        }
    }
}

@Composable
fun SubTopicItem(subTopic: SubTopic, isEditMode: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(subTopic.title, color = AppColors.textPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (isEditMode) {
            IconButton(onClick = onEdit, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Sub-topic", tint = AppColors.textSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Sub-topic", tint = AppColors.textSecondary)
            }
        }
    }
}

@Composable
fun EditSubTopicDialog(subTopic: SubTopic, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(subTopic.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Sub-topic", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Sub-topic title") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(item: Any, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val title = if (item is UnitItem) "Delete Unit?" else "Delete Sub-topic?"
    val text = if (item is UnitItem) "Are you sure you want to delete the unit '${item.title}' and all its sub-topics?"
    else "Are you sure you want to delete the sub-topic '${(item as SubTopic).title}'?"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


// --- Other composables (CourseHeader, etc.) remain the same ---
@Composable
fun CourseHeader() {
    Column {
        Text(
            "Introduction to Machine Learning",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Master the fundamentals of machine learning with hands-on projects and real-world applications. Learn algorithms, data preprocessing, and model evaluation.",
            fontSize = 16.sp,
            color = AppColors.textSecondary,
            lineHeight = 24.sp
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun CourseOutlineScreenPreview() {
    CourseOutlineScreen(onNavigateBack = {}, onGenerateContent = {})
}
