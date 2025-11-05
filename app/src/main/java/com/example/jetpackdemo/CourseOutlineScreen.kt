package com.example.jetpackdemo

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.data.model.CourseOutline
import com.example.jetpackdemo.data.model.OutlineUnit
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.ui.viewmodel.CourseViewModel
import com.example.jetpackdemo.ui.viewmodel.Resource

// --- Data Classes with unique IDs for state management ---
data class SubTopic(val id: Int, var title: String)

data class UnitItem(
    val id: Int,
    var title: String,
    val lessonCount: Int,
    var subTopics: List<SubTopic>
)

// --- Mock Data that can be modified ---
private fun getMockUnits(): List<UnitItem> = listOf(
    UnitItem(1, "Unit 1: Getting Started", 3, listOf(
        SubTopic(101, "What is Machine Learning?"),
        SubTopic(102, "Types of ML Algorithms"),
        SubTopic(103, "Setting Up Your Environment")
    )),
    UnitItem(2, "Unit 2: Data Preprocessing", 4, listOf(
        SubTopic(201, "Data Cleaning"),
        SubTopic(202, "Feature Scaling"),
        SubTopic(203, "Handling Missing Values")
    )),
    UnitItem(3, "Unit 3: Core Algorithms", 5,listOf(
        SubTopic(301, "Linear Regression"),
        SubTopic(302, "Logistic Regression"),
        SubTopic(303, "Decision Trees")
    ))
)

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseOutlineScreen(courseViewModel: CourseViewModel, onNavigateBack: () -> Unit, onGenerateContent: () -> Unit) {
//    val isLoading by courseViewModel.isLoading.collectAsState()
//    val outline by courseViewModel.outlineState.collectAsState()
//    var units by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//
//    var expandedUnitId by remember { mutableStateOf(units.firstOrNull()?.id) }
//    var isEditMode by remember { mutableStateOf(false) }
//    var tempUnits by remember { mutableStateOf(units) }
//
//    // State for dialogs
//    var subTopicToEdit by remember { mutableStateOf<SubTopic?>(null) }
//    var itemToDelete by remember { mutableStateOf<Any?>(null) }
//
//
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = { Text("Review Course Outline", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
//                    }
//                },
//                actions = {
//                    if (isEditMode) {
//                        IconButton(onClick = {
//                            units = tempUnits
//                            isEditMode = false
//                        }) { Icon(Icons.Default.Check, contentDescription = "Save", tint = AppColors.progressGreen) }
//
//                        IconButton(onClick = {
//                            tempUnits = units
//                            isEditMode = false
//                        }) { Icon(Icons.Default.Close, contentDescription = "Cancel", tint = AppColors.textSecondary) }
//                    } else {
//                        TextButton(onClick = {
//                            tempUnits = units.map { it.copy(subTopics = it.subTopics.map { st -> st.copy() }) }
//                            isEditMode = true
//                        }) {
//                            Text("Edit", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
//                        }
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        bottomBar = {
//            BottomAppBar(
//                containerColor = AppColors.surface,
//                tonalElevation = 8.dp
//            ) {
//                Button(
//                    onClick = onGenerateContent,
//                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(56.dp),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
//                ) {
//                    Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.onPrimary)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Generate Course Content", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.onPrimary)
//                }
//            }
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier.fillMaxSize().padding(paddingValues),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            item {
//                CourseHeader()
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//            items(if (isEditMode) tempUnits else units) { unit ->
//                UnitCard(
//                    unit = unit,
//                    isExpanded = expandedUnitId == unit.id,
//                    isEditMode = isEditMode,
//                    onExpand = { expandedUnitId = if (expandedUnitId == unit.id) null else unit.id },
//                    onEditSubTopic = { subTopic -> subTopicToEdit = subTopic },
//                    onDeleteSubTopic = { subTopic -> itemToDelete = subTopic },
//                    onDeleteUnit = { itemToDelete = unit }
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
//
//        // --- Dialogs ---
//        if (subTopicToEdit != null) {
//            EditSubTopicDialog(
//                subTopic = subTopicToEdit!!,
//                onDismiss = { subTopicToEdit = null },
//                onSave = { updatedTitle ->
//                    tempUnits = tempUnits.map { unit ->
//                        unit.copy(subTopics = unit.subTopics.map {
//                            if (it.id == subTopicToEdit!!.id) it.copy(title = updatedTitle) else it
//                        })
//                    }
//                    subTopicToEdit = null
//                }
//            )
//        }
//
//        if (itemToDelete != null) {
//            DeleteConfirmationDialog(
//                item = itemToDelete!!,
//                onDismiss = { itemToDelete = null },
//                onConfirm = {
//                    when (itemToDelete) {
//                        is UnitItem -> {
//                            tempUnits = tempUnits.filterNot { it.id == (itemToDelete as UnitItem).id }
//                        }
//                        is SubTopic -> {
//                            tempUnits = tempUnits.map { unit ->
//                                unit.copy(subTopics = unit.subTopics.filterNot { it.id == (itemToDelete as SubTopic).id })
//                            }
//                        }
//                    }
//                    itemToDelete = null
//                }
//            )
//        }
//    }
//}
// CODEEEE
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseOutlineScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit,
//    onGenerateContent: () -> Unit
//) {
//    val context = LocalContext.current
//
//    val courseId by courseViewModel.courseId.collectAsState()
//    val outline by courseViewModel.outlineState.collectAsState()
//    val isLoading by courseViewModel.isLoading.collectAsState()
//    val updateOutlineResult by courseViewModel.updateOutline.collectAsState()
//
//    var expandedUnitId by remember { mutableStateOf<Int?>(null) }
//    var isEditMode by remember { mutableStateOf(false) }
//    var tempUnits by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//    var units by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//
//    // Dialog state
//    var subTopicToEdit by remember { mutableStateOf<SubTopic?>(null) }
//    var itemToDelete by remember { mutableStateOf<Any?>(null) }
//
//    // When update result changes, show toast on success or error
//    LaunchedEffect(updateOutlineResult) {
//        updateOutlineResult?.let { resource ->
//            when (resource) {
//                is Resource.Success -> {
//                    Toast.makeText(context, "Outline updated successfully", Toast.LENGTH_SHORT).show()
//                    isEditMode = false
//                    // Update units to the saved state
//                    outline?.let {
//                        // You could refresh or update your local state here if needed
//                    }
//                }
//                is Resource.Error -> {
//                    Toast.makeText(context, resource.message ?: "Failed to update outline", Toast.LENGTH_SHORT).show()
//                }
//                else -> {}
//            }
//        }
//    }
//
//
//    // Trigger data update when outline is available
//    LaunchedEffect(outline) {
//        outline?.let {
//            val convertedUnits = it.units.mapIndexed { index, unit ->
//                UnitItem(
//                    id = index,
//                    title = unit.title,
//                    lessonCount = unit.subtopics.count(),
//                    subTopics = unit.subtopics.mapIndexed { subIndex, title ->
//                        SubTopic(id = index * 100 + subIndex, title = title)
//                    }
//                )
//            }
//            units = convertedUnits
//            tempUnits = convertedUnits
//            expandedUnitId = convertedUnits.firstOrNull()?.id
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = { Text("Review Course Outline", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
//                    }
//                },
//                actions = {
//                    if (isEditMode) {
//                        IconButton(onClick = {
////                            val updatedOutline = outline?.copy(
////                                units = tempUnits.mapIndexed { index, unitItem ->  // you can use index for position
////                                    OutlineUnit(
////                                        position = index,  // assign correct position
////                                        title = unitItem.title,
////                                        subtopics = unitItem.subTopics.map { it.title }
////                                    )
////                                }
////                            )
////                            val units = outline?.units?.mapIndexed { index, unit ->
////                                OutlineUnit(
////                                    title = unit.title,  // this should be fine if it's just a String
////                                    position = index + 1,
////                                    subtopics = unit.subtopics.map { it.title }  // Correctly map to a List<String>
////                                )
////                            }
////
////// Create the CourseOutline object
////                            val request = units?.let {
////                                outline?.let { it1 ->
////                                    CourseOutline(
////                                        courseTitle = it1.courseTitle,
////                                        difficulty = outline!!.difficulty,
////                                        units = it  // This should be a List<OutlineUnit> now
////                                    )
////                                }
////                            }
//                            // Validate units
//                            val validUnits = units.filter { unit ->
//                                unit.title.length >= 3 && unit.subTopics.size in 1..6 && unit.subTopics.all { it.title.length >= 2 }
//                            }
//                            if (validUnits.size != units.size) {
//                                Toast.makeText(context, "Some units or subtopics are invalid", Toast.LENGTH_SHORT).show()
//                                return@IconButton
//                            }
//
//                            val updatedOutline = CourseOutline(
//                                courseTitle = outline?.courseTitle ?: "",
//                                difficulty = outline?.difficulty,
//                                units = validUnits.mapIndexed { index, unit ->
//                                    OutlineUnit(
//                                        title = unit.title,
//                                        position = index + 1,
//                                        subtopics = unit.subTopics.map { it.title }
//                                    )
//                                }
//                            )
//
//
//// Assuming you want to update the outline without regeneration
////                            if (updatedOutline != null && !courseId.isNullOrEmpty()) {
////                                courseViewModel.updateCourseOutlineBeforeGeneration(courseId!!, updatedOutline, regenerate = false)
////                            }
//                            if (!courseId.isNullOrEmpty()) {
//                                courseViewModel.updateCourseOutlineBeforeGeneration(courseId!!, updatedOutline, regenerate = false)
//                            }
//
//                            units = tempUnits
//                            isEditMode = false
//                        }) {
//                            Icon(Icons.Default.Check, contentDescription = "Save", tint = AppColors.progressGreen)
//                        }
//                        IconButton(onClick = {
//                            tempUnits = units
//                            isEditMode = false
//                        }) {
//                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = AppColors.textSecondary)
//                        }
//                    } else {
//                        TextButton(onClick = {
//                            tempUnits = units.map { it.copy(subTopics = it.subTopics.map { st -> st.copy() }) }
//                            isEditMode = true
//                        }) {
//                            Text("Edit", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
//                        }
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        bottomBar = {
//            if (!isLoading && outline != null) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp
//                ) {
//                    Button(
//                        onClick = onGenerateContent,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 8.dp)
//                            .height(56.dp),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
//                    ) {
//                        Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.onPrimary)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            "Generate Course Content",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = AppColors.onPrimary
//                        )
//                    }
//                }
//            }
//        }
//    ) { paddingValues ->
//        when {
//            isLoading -> {
//                // Show loading spinner
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator(color = AppColors.primary)
//                }
//            }
//
//            outline == null -> {
//                // Show empty or error state
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    Text("Failed to load outline", color = AppColors.textSecondary)
//                }
//            }
//
//            else -> {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues),
//                    contentPadding = PaddingValues(16.dp)
//                ) {
//                    item {
//                        outline!!.difficulty?.let { CourseHeader(outline!!.courseTitle, it) }
//                        Spacer(modifier = Modifier.height(24.dp))
//                    }
//
//                    items(if (isEditMode) tempUnits else units) { unit ->
//                        UnitCard(
//                            unit = unit,
//                            isExpanded = expandedUnitId == unit.id,
//                            isEditMode = isEditMode,
//                            onExpand = { expandedUnitId = if (expandedUnitId == unit.id) null else unit.id },
//                            onEditSubTopic = { subTopicToEdit = it }, // This should open the dialog
//                            onDeleteSubTopic = { itemToDelete = it },
//                            onDeleteUnit = { itemToDelete = unit }
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//                // Show the Edit Sub-topic Dialog if `subTopicToEdit` is not null
//                subTopicToEdit?.let {
//                    EditSubTopicDialog(
//                        subTopic = it,
//                        onDismiss = { subTopicToEdit = null },
//                        onSave = { updatedTitle ->
//                            tempUnits = tempUnits.map { unit ->
//                                unit.copy(subTopics = unit.subTopics.map {
//                                    if (it.id == subTopicToEdit!!.id) it.copy(title = updatedTitle) else it
//                                })
//                            }
//                            subTopicToEdit = null // Close the dialog after saving
//                        }
//                    )
//                }
//
//                // --- Dialogs ---
////                if (subTopicToEdit != null) {
////                    EditSubTopicDialog(
////                        subTopic = subTopicToEdit!!,
////                        onDismiss = { subTopicToEdit = null },
////                        onSave = { updatedTitle ->
////                            tempUnits = tempUnits.map { unit ->
////                                unit.copy(subTopics = unit.subTopics.map {
////                                    if (it.id == subTopicToEdit!!.id) it.copy(title = updatedTitle) else it
////                                })
////                            }
////                            subTopicToEdit = null
////                        }
////                    )
////                }
//
//                if (itemToDelete != null) {
//                    DeleteConfirmationDialog(
//                        item = itemToDelete!!,
//                        onDismiss = { itemToDelete = null },
//                        onConfirm = {
//                            when (itemToDelete) {
//                                is UnitItem -> {
//                                    tempUnits = tempUnits.filterNot { it.id == (itemToDelete as UnitItem).id }
//                                }
//
//                                is SubTopic -> {
//                                    tempUnits = tempUnits.map { unit ->
//                                        unit.copy(subTopics = unit.subTopics.filterNot { it.id == (itemToDelete as SubTopic).id })
//                                    }
//                                }
//                            }
//                            itemToDelete = null
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
/// HALF WORKING
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseOutlineScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit,
//    onGenerateContent: () -> Unit
//) {
//    val context = LocalContext.current
//
//    val courseId by courseViewModel.courseId.collectAsState()
//    val outline by courseViewModel.outlineState.collectAsState()
//    val isLoading by courseViewModel.isLoading.collectAsState()
//    val updateOutlineResult by courseViewModel.updateOutline.collectAsState()
//
//    var expandedUnitId by remember { mutableStateOf<Int?>(null) }
//    var isEditMode by remember { mutableStateOf(false) }
//    var tempUnits by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//    var units by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//
//    // Dialog state
//    var subTopicToEdit by remember { mutableStateOf<SubTopic?>(null) }
//    var itemToDelete by remember { mutableStateOf<Any?>(null) }
//
//    // When update result changes, show toast on success or error
//    LaunchedEffect(updateOutlineResult) {
//        updateOutlineResult?.let { resource ->
//            when (resource) {
//                is Resource.Success -> {
//                    Toast.makeText(context, "Outline updated successfully", Toast.LENGTH_SHORT).show()
//                    isEditMode = false
//                    // Update units to the saved state
//                    outline?.let {
//                        units = it.units.mapIndexed { index, unit ->
//                            UnitItem(
//                                id = index,
//                                title = unit.title,
//                                lessonCount = unit.subtopics.count(),
//                                subTopics = unit.subtopics.mapIndexed { subIndex, title ->
//                                    SubTopic(id = index * 100 + subIndex, title = title)
//                                }
//                            )
//                        }
//                    }
//                }
//                is Resource.Error -> {
//                    Toast.makeText(context, resource.message ?: "Failed to update outline", Toast.LENGTH_SHORT).show()
//                }
//                else -> {}
//            }
//        }
//    }
//
//    // Trigger data update when outline is available
//    LaunchedEffect(outline) {
//        outline?.let {
//            val convertedUnits = it.units.mapIndexed { index, unit ->
//                UnitItem(
//                    id = index,
//                    title = unit.title,
//                    lessonCount = unit.subtopics.count(),
//                    subTopics = unit.subtopics.mapIndexed { subIndex, title ->
//                        SubTopic(id = index * 100 + subIndex, title = title)
//                    }
//                )
//            }
//            units = convertedUnits
//            tempUnits = convertedUnits
//            expandedUnitId = convertedUnits.firstOrNull()?.id
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = { Text("Review Course Outline", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
//                    }
//                },
//                actions = {
//                    if (isEditMode) {
//                        IconButton(onClick = {
//                            val validUnits = units.filter { unit ->
//                                unit.title.length >= 3 && unit.subTopics.size in 1..6 && unit.subTopics.all { it.title.length >= 2 }
//                            }
//                            if (validUnits.size != units.size) {
//                                Toast.makeText(context, "Some units or subtopics are invalid", Toast.LENGTH_SHORT).show()
//                                return@IconButton
//                            }
//
//                            val updatedOutline = CourseOutline(
//                                courseTitle = outline?.courseTitle ?: "",
//                                difficulty = outline?.difficulty,
//                                units = validUnits.mapIndexed { index, unit ->
//                                    OutlineUnit(
//                                        title = unit.title,
//                                        position = index + 1,
//                                        subtopics = unit.subTopics.map { it.title }
//                                    )
//                                }
//                            )
//
//                            if (!courseId.isNullOrEmpty()) {
//                                courseViewModel.updateCourseOutlineBeforeGeneration(courseId!!, updatedOutline, regenerate = false)
//                            }
//
//                            units = tempUnits
//                            isEditMode = false
//                        }) {
//                            Icon(Icons.Default.Check, contentDescription = "Save", tint = AppColors.progressGreen)
//                        }
//                        IconButton(onClick = {
//                            tempUnits = units
//                            isEditMode = false
//                        }) {
//                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = AppColors.textSecondary)
//                        }
//                    } else {
//                        TextButton(onClick = {
//                            tempUnits = units.map { it.copy(subTopics = it.subTopics.map { st -> st.copy() }) }
//                            isEditMode = true
//                        }) {
//                            Text("Edit", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
//                        }
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        bottomBar = {
//            if (!isLoading && outline != null) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp
//                ) {
//                    Button(
//                        onClick = onGenerateContent,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 8.dp)
//                            .height(56.dp),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
//                    ) {
//                        Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.onPrimary)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            "Generate Course Content",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = AppColors.onPrimary
//                        )
//                    }
//                }
//            }
//        }
//    ) { paddingValues ->
//        when {
//            isLoading -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator(color = AppColors.primary)
//                }
//            }
//
//            outline == null -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    Text("Failed to load outline", color = AppColors.textSecondary)
//                }
//            }
//
//            else -> {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues),
//                    contentPadding = PaddingValues(16.dp)
//                ) {
//                    item {
//                        outline!!.difficulty?.let { CourseHeader(outline!!.courseTitle, it) }
//                        Spacer(modifier = Modifier.height(24.dp))
//                    }
//
//                    items(if (isEditMode) tempUnits else units) { unit ->
//                        UnitCard(
//                            unit = unit,
//                            isExpanded = expandedUnitId == unit.id,
//                            isEditMode = isEditMode,
//                            onExpand = { expandedUnitId = if (expandedUnitId == unit.id) null else unit.id },
//                            onEditSubTopic = { subTopicToEdit = it },
//                            onDeleteSubTopic = { itemToDelete = it },
//                            onDeleteUnit = { itemToDelete = unit }
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//                // Dialogs
//                if (subTopicToEdit != null) {
//                    EditSubTopicDialog(
//                        subTopic = subTopicToEdit!!,
//                        onDismiss = { subTopicToEdit = null },
//                        onSave = { updatedTitle ->
//                            tempUnits = tempUnits.map { unit ->
//                                unit.copy(subTopics = unit.subTopics.map {
//                                    if (it.id == subTopicToEdit!!.id) it.copy(title = updatedTitle) else it
//                                })
//                            }
//                            subTopicToEdit = null
//                        }
//                    )
//                }
//
//                if (itemToDelete != null) {
//                    DeleteConfirmationDialog(
//                        item = itemToDelete!!,
//                        onDismiss = { itemToDelete = null },
//                        onConfirm = {
//                            when (itemToDelete) {
//                                is UnitItem -> {
//                                    tempUnits = tempUnits.filterNot { it.id == (itemToDelete as UnitItem).id }
//                                }
//
//                                is SubTopic -> {
//                                    tempUnits = tempUnits.map { unit ->
//                                        unit.copy(subTopics = unit.subTopics.filterNot { it.id == (itemToDelete as SubTopic).id })
//                                    }
//                                }
//                            }
//                            itemToDelete = null
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
// NOT WORKING
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseOutlineScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit,
//    onGenerateContent: () -> Unit
//) {
//    val context = LocalContext.current
//
//    val courseId by courseViewModel.courseId.collectAsState()
//    val outline by courseViewModel.outlineState.collectAsState()
//    val isLoading by courseViewModel.isLoading.collectAsState()
//    val updateOutlineResult by courseViewModel.updateOutline.collectAsState()
//
//    var expandedUnitId by remember { mutableStateOf<Int?>(null) }
//    var isEditMode by remember { mutableStateOf(false) }
//    var tempUnits by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//    var units by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//
//    // Dialog state
//    var subTopicToEdit by remember { mutableStateOf<SubTopic?>(null) }
//    var itemToDelete by remember { mutableStateOf<Any?>(null) }
//
//    // When update result changes, show toast on success or error
//    LaunchedEffect(updateOutlineResult) {
//        updateOutlineResult?.let { resource ->
//            when (resource) {
//                is Resource.Success -> {
//                    Toast.makeText(context, "Outline updated successfully", Toast.LENGTH_SHORT).show()
//                    isEditMode = false
//                    // Ensure units are updated from outline
//                    outline?.let {
//                        val convertedUnits = it.units.mapIndexed { index, unit ->
//                            UnitItem(
//                                id = index,
//                                title = unit.title,
//                                lessonCount = unit.subtopics.count(),
//                                subTopics = unit.subtopics.mapIndexed { subIndex, title ->
//                                    SubTopic(id = index * 100 + subIndex, title = title)
//                                }
//                            )
//                        }
//                        units = convertedUnits
//                        tempUnits = convertedUnits
//                    }
//                }
//                is Resource.Error -> {
//                    Toast.makeText(context, resource.message ?: "Failed to update outline", Toast.LENGTH_SHORT).show()
//                }
//                else -> {}
//            }
//        }
//    }
//
//    // Trigger data update when outline is available
//    LaunchedEffect(outline) {
//        outline?.let {
//            val convertedUnits = it.units.mapIndexed { index, unit ->
//                UnitItem(
//                    id = index,
//                    title = unit.title,
//                    lessonCount = unit.subtopics.count(),
//                    subTopics = unit.subtopics.mapIndexed { subIndex, title ->
//                        SubTopic(id = index * 100 + subIndex, title = title)
//                    }
//                )
//            }
//            units = convertedUnits
//            tempUnits = convertedUnits
//            expandedUnitId = convertedUnits.firstOrNull()?.id
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = { Text("Review Course Outline", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
//                    }
//                },
//                actions = {
//                    if (isEditMode) {
//                        IconButton(onClick = {
//                            // Validation before saving
//                            val validUnits = units.filter { unit ->
//                                unit.title.length >= 3 && unit.subTopics.size in 1..6 && unit.subTopics.all { it.title.length >= 2 }
//                            }
//                            if (validUnits.size != units.size) {
//                                Toast.makeText(context, "Some units or subtopics are invalid", Toast.LENGTH_SHORT).show()
//                                return@IconButton
//                            }
//
//                            val updatedOutline = CourseOutline(
//                                courseTitle = outline?.courseTitle ?: "",
//                                difficulty = outline?.difficulty,
//                                units = validUnits.mapIndexed { index, unit ->
//                                    OutlineUnit(
//                                        title = unit.title,
//                                        position = index + 1,
//                                        subtopics = unit.subTopics.map { it.title }
//                                    )
//                                }
//                            )
//
//                            if (!courseId.isNullOrEmpty()) {
//                                courseViewModel.updateCourseOutlineBeforeGeneration(courseId!!, updatedOutline, regenerate = false)
//                            }
//
//                            units = tempUnits
//                            isEditMode = false
//                        }) {
//                            Icon(Icons.Default.Check, contentDescription = "Save", tint = AppColors.progressGreen)
//                        }
//                        IconButton(onClick = {
//                            tempUnits = units
//                            isEditMode = false
//                        }) {
//                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = AppColors.textSecondary)
//                        }
//                    } else {
//                        TextButton(onClick = {
//                            tempUnits = units.map { it.copy(subTopics = it.subTopics.map { st -> st.copy() }) }
//                            isEditMode = true
//                        }) {
//                            Text("Edit", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
//                        }
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        bottomBar = {
//            if (!isLoading && outline != null) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp
//                ) {
//                    Button(
//                        onClick = onGenerateContent,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 8.dp)
//                            .height(56.dp),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
//                    ) {
//                        Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.onPrimary)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            "Generate Course Content",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = AppColors.onPrimary
//                        )
//                    }
//                }
//            }
//        }
//    ) { paddingValues ->
//        when {
//            isLoading -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator(color = AppColors.primary)
//                }
//            }
//
//            outline == null -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    Text("Failed to load outline", color = AppColors.textSecondary)
//                }
//            }
//
//            else -> {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues),
//                    contentPadding = PaddingValues(16.dp)
//                ) {
//                    item {
//                        outline!!.difficulty?.let { CourseHeader(outline!!.courseTitle, it) }
//                        Spacer(modifier = Modifier.height(24.dp))
//                    }
//
//                    items(if (isEditMode) tempUnits else units) { unit ->
//                        UnitCard(
//                            unit = unit,
//                            isExpanded = expandedUnitId == unit.id,
//                            isEditMode = isEditMode,
//                            onExpand = { expandedUnitId = if (expandedUnitId == unit.id) null else unit.id },
//                            onEditSubTopic = { subTopicToEdit = it },
//                            onDeleteSubTopic = { itemToDelete = it },
//                            onDeleteUnit = { itemToDelete = unit }
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//                // --- Edit and Add Subtopic Dialog ---
//                if (subTopicToEdit != null) {
//                    EditSubTopicDialog(
//                        subTopic = subTopicToEdit!!,
//                        onDismiss = { subTopicToEdit = null },
//                        onSave = { updatedTitle ->
//                            if (subTopicToEdit!!.id == -1) {  // New subtopic
//                                // Add new subtopic to the selected unit
//                                tempUnits = tempUnits.map { unit ->
//                                    if (unit.id == expandedUnitId) {
//                                        unit.copy(subTopics = unit.subTopics + SubTopic(id = unit.subTopics.size + 1, title = updatedTitle))
//                                    } else unit
//                                }
//                            } else {  // Edit existing subtopic
//                                tempUnits = tempUnits.map { unit ->
//                                    unit.copy(subTopics = unit.subTopics.map {
//                                        if (it.id == subTopicToEdit!!.id) it.copy(title = updatedTitle) else it
//                                    })
//                                }
//                            }
//                            subTopicToEdit = null
//                        }
//                    )
//                }
//
//                // --- Delete Confirmation Dialog ---
//                if (itemToDelete != null) {
//                    DeleteConfirmationDialog(
//                        item = itemToDelete!!,
//                        onDismiss = { itemToDelete = null },
//                        onConfirm = {
//                            when (itemToDelete) {
//                                is UnitItem -> {
//                                    tempUnits = tempUnits.filterNot { it.id == (itemToDelete as UnitItem).id }
//                                }
//
//                                is SubTopic -> {
//                                    tempUnits = tempUnits.map { unit ->
//                                        unit.copy(subTopics = unit.subTopics.filterNot { it.id == (itemToDelete as SubTopic).id })
//                                    }
//                                }
//                            }
//                            // Now update the course outline after deletion
//                            if (!courseId.isNullOrEmpty()) {
//                                courseViewModel.updateCourseOutlineBeforeGeneration(courseId!!,
//                                    CourseOutline(courseTitle = outline!!.courseTitle, difficulty = outline!!.difficulty,
//                                        units = tempUnits.map { unit ->
//                                            OutlineUnit(title = unit.title, position = unit.id, subtopics = unit.subTopics.map { it.title }) }), regenerate = false
//                                )
//                            }
//                            itemToDelete = null
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//
//@Composable
//fun CourseHeader(title: String, description: String) {
//    Column {
//        Text(
//            title,
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            color = AppColors.textPrimary
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            description,
//            fontSize = 16.sp,
//            color = AppColors.textSecondary,
//            lineHeight = 24.sp
//        )
//    }
//}
//
//
//@Composable
//fun UnitCard(
//    unit: UnitItem,
//    isExpanded: Boolean,
//    isEditMode: Boolean,
//    onExpand: () -> Unit,
//    onEditSubTopic: (SubTopic) -> Unit,
//    onDeleteSubTopic: (SubTopic) -> Unit,
//    onDeleteUnit: () -> Unit
//) {
//    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotation")
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column {
//            Row(
//                modifier = Modifier.clickable(onClick = onExpand).padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(unit.title, fontWeight = FontWeight.Bold, color = AppColors.textPrimary, fontSize = 18.sp)
//                    Spacer(modifier = Modifier.height(4.dp))
//                }
//                if (isEditMode) {
//                    IconButton(onClick = onDeleteUnit, modifier = Modifier.size(24.dp)) {
//                        Icon(Icons.Default.Delete, contentDescription = "Delete Unit", tint = AppColors.textSecondary)
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                }
//                Icon(
//                    imageVector = Icons.Default.KeyboardArrowDown,
//                    contentDescription = "Expand",
//                    tint = AppColors.textSecondary,
//                    modifier = Modifier.rotate(rotationAngle)
//                )
//            }
//
//            AnimatedVisibility(visible = isExpanded) {
//                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
//                    HorizontalDivider(color = AppColors.background)
//                    Spacer(modifier = Modifier.height(16.dp))
//                    unit.subTopics.forEach { subTopic ->
//                        SubTopicItem(subTopic, isEditMode, onEdit = { onEditSubTopic(subTopic) }, onDelete = { onDeleteSubTopic(subTopic) })
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SubTopicItem(subTopic: SubTopic, isEditMode: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
//    Row(
//        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(subTopic.title, color = AppColors.textPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
//        if (isEditMode) {
//            IconButton(onClick = onEdit, modifier = Modifier.size(20.dp)) {
//                Icon(Icons.Default.Edit, contentDescription = "Edit Sub-topic", tint = AppColors.textSecondary)
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
//                Icon(Icons.Default.Delete, contentDescription = "Delete Sub-topic", tint = AppColors.textSecondary)
//            }
//        }
//    }
//}
//
//@Composable
    //fun EditSubTopicDialog(subTopic: SubTopic, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    //    var text by remember { mutableStateOf(subTopic.title) }
    //
    //    AlertDialog(
    //        onDismissRequest = onDismiss,
    //        title = { Text("Edit Sub-topic", fontWeight = FontWeight.Bold) },
    //        text = {
    //            OutlinedTextField(
    //                value = text,
    //                onValueChange = { text = it },
    //                label = { Text("Sub-topic title") },
    //                modifier = Modifier.fillMaxWidth()
    //            )
    //        },
    //        confirmButton = {
    //            Button(onClick = { onSave(text) }) { Text("Save") }
    //        },
    //        dismissButton = {
    //            TextButton(onClick = onDismiss) { Text("Cancel") }
    //        }
    //    )
    //}
    //
//@Composable
//fun DeleteConfirmationDialog(item: Any, onDismiss: () -> Unit, onConfirm: () -> Unit) {
//    val title = if (item is UnitItem) "Delete Unit?" else "Delete Sub-topic?"
//    val text = if (item is UnitItem) "Are you sure you want to delete the unit '${item.title}' and all its sub-topics?"
//    else "Are you sure you want to delete the sub-topic '${(item as SubTopic).title}'?"
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(title, fontWeight = FontWeight.Bold) },
//        text = { Text(text) },
//        confirmButton = {
//            Button(
//                onClick = onConfirm,
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
//            ) { Text("Delete") }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) { Text("Cancel") }
//        }
//    )
//}
// WORKING 2 TIMES
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseOutlineScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit,
//    onGenerateContent: () -> Unit
//) {
//    val context = LocalContext.current
//
//    val courseId by courseViewModel.courseId.collectAsState()
//    val outline by courseViewModel.outlineState.collectAsState()
//    val isLoading by courseViewModel.isLoading.collectAsState()
//    val updateOutlineResult by courseViewModel.updateOutline.collectAsState()
//
//    var expandedUnitId by remember { mutableStateOf<Int?>(null) }
//    var isEditMode by remember { mutableStateOf(false) }
//    var tempUnits by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//    var units by remember { mutableStateOf<List<UnitItem>>(emptyList()) }
//
//    // Dialog state for delete confirmation
//    var itemToDelete by remember { mutableStateOf<Any?>(null) }
//
//    // State for edit sub-topic dialog
//    var editingSubTopic by remember { mutableStateOf<SubTopic?>(null) }
//
//    // When update result changes, show toast on success or error
//    LaunchedEffect(updateOutlineResult) {
//        updateOutlineResult?.let { resource ->
//            when (resource) {
//                is Resource.Success -> {
//                    Toast.makeText(context, "Outline updated successfully", Toast.LENGTH_SHORT).show()
//                    isEditMode = false
//                    outline?.let {
//                        // You could refresh or update your local state here if needed
//                    }
//                }
//                is Resource.Error -> {
//                    Toast.makeText(context, resource.message ?: "Failed to update outline", Toast.LENGTH_SHORT).show()
//                }
//                else -> {}
//            }
//        }
//    }
//
//    // Trigger data update when outline is available
//    LaunchedEffect(outline) {
//        outline?.let {
//            val convertedUnits = it.units.mapIndexed { index, unit ->
//                UnitItem(
//                    id = index,
//                    title = unit.title,
//                    lessonCount = unit.subtopics.count(),
//                    subTopics = unit.subtopics.mapIndexed { subIndex, title ->
//                        SubTopic(id = index * 100 + subIndex, title = title)
//                    }
//                )
//            }
//            units = convertedUnits
//            tempUnits = convertedUnits
//            expandedUnitId = convertedUnits.firstOrNull()?.id
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = { Text("Review Course Outline", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
//                    }
//                },
//                actions = {
//                    if (isEditMode) {
//                        IconButton(onClick = {
//                            // Validate units
//                            val validUnits = units.filter { unit ->
//                                unit.title.length >= 3 && unit.subTopics.size in 1..6 && unit.subTopics.all { it.title.length >= 2 }
//                            }
//                            if (validUnits.size != units.size) {
//                                Toast.makeText(context, "Some units or subtopics are invalid", Toast.LENGTH_SHORT).show()
//                                return@IconButton
//                            }
//
//                            val updatedOutline = CourseOutline(
//                                courseTitle = outline?.courseTitle ?: "",
//                                difficulty = outline?.difficulty,
//                                units = validUnits.mapIndexed { index, unit ->
//                                    OutlineUnit(
//                                        title = unit.title,
//                                        position = index + 1,
//                                        subtopics = unit.subTopics.map { it.title }
//                                    )
//                                }
//                            )
//
//                            // Assuming you want to update the outline without regeneration
//                            if (!courseId.isNullOrEmpty()) {
//                                courseViewModel.updateCourseOutlineBeforeGeneration(courseId!!, updatedOutline, regenerate = false)
//                            }
//
//                            units = tempUnits
//                            isEditMode = false
//                        }) {
//                            Icon(Icons.Default.Check, contentDescription = "Save", tint = AppColors.progressGreen)
//                        }
//                        IconButton(onClick = {
//                            tempUnits = units
//                            isEditMode = false
//                        }) {
//                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = AppColors.textSecondary)
//                        }
//                    } else {
//                        TextButton(onClick = {
//                            tempUnits = units.map { it.copy(subTopics = it.subTopics.map { st -> st.copy() }) }
//                            isEditMode = true
//                        }) {
//                            Text("Edit", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
//                        }
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        bottomBar = {
//            if (!isLoading && outline != null) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp
//                ) {
//                    Button(
//                        onClick = onGenerateContent,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 8.dp)
//                            .height(56.dp),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
//                    ) {
//                        Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.onPrimary)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            "Generate Course Content",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = AppColors.onPrimary
//                        )
//                    }
//                }
//            }
//        }
//    ) { paddingValues ->
//        when {
//            isLoading -> {
//                // Show loading spinner
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator(color = AppColors.primary)
//                }
//            }
//
//            outline == null -> {
//                // Show empty or error state
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    Text("Failed to load outline", color = AppColors.textSecondary)
//                }
//            }
//
//            else -> {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues),
//                    contentPadding = PaddingValues(16.dp)
//                ) {
//                    item {
//                        outline!!.difficulty?.let { CourseHeader(outline!!.courseTitle, it) }
//                        Spacer(modifier = Modifier.height(24.dp))
//                    }
//
//                    items(if (isEditMode) tempUnits else units) { unit ->
//                        UnitCard(
//                            unit = unit,
//                            isExpanded = expandedUnitId == unit.id,
//                            isEditMode = isEditMode,
//                            onExpand = { expandedUnitId = if (expandedUnitId == unit.id) null else unit.id },
//                            onDeleteUnit = { itemToDelete = unit },
//                            onDeleteSubTopic = { subTopic -> itemToDelete = subTopic },
//                            onEditSubTopic = { subTopic -> editingSubTopic = subTopic } // This will trigger the edit dialog
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//                // --- Dialogs ---
//                if (itemToDelete != null) {
//                    DeleteConfirmationDialog(
//                        item = itemToDelete!!,
//                        onDismiss = { itemToDelete = null },
//                        onConfirm = {
//                            when (itemToDelete) {
//                                is UnitItem -> {
//                                    tempUnits = tempUnits.filterNot { it.id == (itemToDelete as UnitItem).id }
//                                }
//                                is SubTopic -> {
//                                    tempUnits = tempUnits.map { unit ->
//                                        unit.copy(subTopics = unit.subTopics.filterNot { it.id == (itemToDelete as SubTopic).id })
//                                    }
//                                }
//                                else -> {}
//                            }
//                            itemToDelete = null
//                        }
//                    )
//                }
//
//                // Sub-topic Edit Dialog
//                editingSubTopic?.let { subTopic ->
//                    EditSubTopicDialog(
//                        subTopic = subTopic,
//                        onDismiss = { editingSubTopic = null },
//                        onSave = { newTitle ->
//                            // Update sub-topic title
//                            tempUnits = tempUnits.map { unit ->
//                                if (unit.subTopics.any { it.id == subTopic.id }) {
//                                    unit.copy(
//                                        subTopics = unit.subTopics.map {
//                                            if (it.id == subTopic.id) it.copy(title = newTitle) else it
//                                        }
//                                    )
//                                } else {
//                                    unit
//                                }
//                            }
//                            editingSubTopic = null
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseOutlineScreen(
    courseViewModel: CourseViewModel,
    onNavigateBack: () -> Unit,
    onGenerateContent: () -> Unit
) {
    val context = LocalContext.current

    val courseId by courseViewModel.courseId.collectAsState()
    val outline by courseViewModel.outlineState.collectAsState()
    val isLoading by courseViewModel.isLoading.collectAsState()
    val updateOutlineResult by courseViewModel.updateOutline.collectAsState()

    var expandedUnitId by remember { mutableStateOf<Int?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var tempUnits by remember { mutableStateOf<List<UnitItem>>(emptyList()) }

    // Dialog state for delete confirmation
    var itemToDelete by remember { mutableStateOf<Any?>(null) }

    // State for edit sub-topic dialog
    var editingSubTopic by remember { mutableStateOf<SubTopic?>(null) }

    // State for add sub-topic dialog
    var unitToAddSubTopic by remember { mutableStateOf<UnitItem?>(null) }

    // When update result changes, show toast on success or error
    LaunchedEffect(updateOutlineResult) {
        updateOutlineResult?.let { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(context, "Outline updated successfully", Toast.LENGTH_SHORT).show()
                    isEditMode = false
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message ?: "Failed to update outline", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    // Trigger data update when outline is available
    LaunchedEffect(outline) {
        outline?.let {
            val convertedUnits = it.units.mapIndexed { index, unit ->
                UnitItem(
                    id = index,
                    title = unit.title,
                    lessonCount = unit.subtopics.count(),
                    subTopics = unit.subtopics.mapIndexed { subIndex, title ->
                        SubTopic(id = index * 100 + subIndex, title = title)
                    }
                )
            }
            tempUnits = convertedUnits
            expandedUnitId = convertedUnits.firstOrNull()?.id
        }
    }

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
                            // Validate units
                            val validUnits = tempUnits.filter { unit ->
                                unit.title.length >= 3 && unit.subTopics.size in 1..6 && unit.subTopics.all { it.title.length >= 2 }
                            }
                            if (validUnits.size != tempUnits.size) {
                                Toast.makeText(context, "Some units or subtopics are invalid", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }

                            val updatedOutline = CourseOutline(
                                courseTitle = outline?.courseTitle ?: "",
                                difficulty = outline?.difficulty,
                                units = validUnits.mapIndexed { index, unit ->
                                    OutlineUnit(
                                        title = unit.title,
                                        position = index + 1,
                                        subtopics = unit.subTopics.map { it.title }
                                    )
                                }
                            )

                            // Use the updated outline with tempUnits data
                            if (!courseId.isNullOrEmpty()) {
                                courseViewModel.updateCourseOutlineBeforeGeneration(courseId!!, updatedOutline, regenerate = false)
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = AppColors.progressGreen)
                        }
                        IconButton(onClick = {
                            // Reset tempUnits to original outline when canceling
                            outline?.let {
                                val convertedUnits = it.units.mapIndexed { index, unit ->
                                    UnitItem(
                                        id = index,
                                        title = unit.title,
                                        lessonCount = unit.subtopics.count(),
                                        subTopics = unit.subtopics.mapIndexed { subIndex, title ->
                                            SubTopic(id = index * 100 + subIndex, title = title)
                                        }
                                    )
                                }
                                tempUnits = convertedUnits
                            }
                            isEditMode = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = AppColors.textSecondary)
                        }
                    } else {
                        TextButton(onClick = {
                            // Create a deep copy of the current units for editing
                            tempUnits = tempUnits.map { unit ->
                                unit.copy(subTopics = unit.subTopics.map { st -> st.copy() })
                            }
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
            if (!isLoading && outline != null) {
                BottomAppBar(
                    containerColor = AppColors.surface,
                    tonalElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            if (!courseId.isNullOrEmpty()) {
                                // Start content generation
                                // NEW: Start STREAMING generation with provider
                                courseViewModel.startStreamingGeneration(
                                    courseId = courseId!!,
//                                    provider = "Cerebras",  // CHANGE LATER
                                    provider = "Groq",  // CHANGE LATER
//                                    provider = "Gemini",  // CHANGE LATER
                                    model = null            // Optional
                                )
                                // Navigate immediately
                                onGenerateContent()
                            } else {
                                Toast.makeText(context, "Course ID not available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Generate Course Content",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.onPrimary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }

            outline == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Failed to load outline", color = AppColors.textSecondary)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        outline!!.difficulty?.let { CourseHeader(outline!!.courseTitle, it) }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    items(tempUnits) { unit ->
                        UnitCard(
                            unit = unit,
                            isExpanded = expandedUnitId == unit.id,
                            isEditMode = isEditMode,
                            onExpand = { expandedUnitId = if (expandedUnitId == unit.id) null else unit.id },
                            onDeleteUnit = { itemToDelete = unit },
                            onDeleteSubTopic = { subTopic -> itemToDelete = subTopic },
                            onEditSubTopic = { subTopic -> editingSubTopic = subTopic },
                            onAddSubTopic = { unitToAddSubTopic = unit } // New callback for adding sub-topic
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // --- Dialogs ---
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
                                else -> {}
                            }
                            itemToDelete = null
                        }
                    )
                }

                editingSubTopic?.let { subTopic ->
                    EditSubTopicDialog(
                        subTopic = subTopic,
                        onDismiss = { editingSubTopic = null },
                        onSave = { newTitle ->
                            tempUnits = tempUnits.map { unit ->
                                if (unit.subTopics.any { it.id == subTopic.id }) {
                                    unit.copy(
                                        subTopics = unit.subTopics.map {
                                            if (it.id == subTopic.id) it.copy(title = newTitle) else it
                                        }
                                    )
                                } else {
                                    unit
                                }
                            }
                            editingSubTopic = null
                        }
                    )
                }

                // Add Sub-topic Dialog
                unitToAddSubTopic?.let { unit ->
                    AddSubTopicDialog(
                        unit = unit,
                        onDismiss = { unitToAddSubTopic = null },
                        onAdd = { newSubTopicTitle ->
                            // Generate a unique ID for the new sub-topic
                            val newId = (unit.subTopics.maxByOrNull { it.id }?.id ?: unit.id * 100) + 1
                            val newSubTopic = SubTopic(id = newId, title = newSubTopicTitle)

                            tempUnits = tempUnits.map { u ->
                                if (u.id == unit.id) {
                                    u.copy(subTopics = u.subTopics + newSubTopic)
                                } else {
                                    u
                                }
                            }
                            unitToAddSubTopic = null
                        }
                    )
                }
            }
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
    onDeleteUnit: () -> Unit,
    onAddSubTopic: () -> Unit // New parameter for adding sub-topic
) {
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable(onClick = onExpand)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        unit.title,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        fontSize = 18.sp
                    )
                }
                if (isEditMode) {
                    // Delete Unit Button
                    IconButton(onClick = onDeleteUnit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Unit", tint = AppColors.textSecondary)
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = AppColors.textSecondary,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    unit.subTopics.forEach { subTopic ->
                        SubTopicItem(
                            subTopic = subTopic,
                            isEditMode = isEditMode,
                            onEdit = { onEditSubTopic(subTopic) },
                            onDelete = { onDeleteSubTopic(subTopic) }
                        )
                    }

                    // Add Sub-topic Button (only visible in edit mode)
                    if (isEditMode) {
                        Button(
                            onClick = onAddSubTopic,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Sub-topic", tint = AppColors.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Sub-topic", color = AppColors.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubTopicDialog(
    unit: UnitItem,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Sub-topic to ${unit.title}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Sub-topic title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Minimum 2 characters required",
                    fontSize = 12.sp,
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.length >= 2) {
                        onAdd(text)
                    }
                },
                enabled = text.length >= 2
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// The rest of your composables (SubTopicItem, EditSubTopicDialog, DeleteConfirmationDialog, etc.) remain the same
@Composable
fun CourseHeader(title: String, description: String) {
    Column {
        Text(
            title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            description,
            fontSize = 16.sp,
            color = AppColors.textSecondary,
            lineHeight = 24.sp
        )
    }
}

//@Composable
//fun UnitCard(
//    unit: UnitItem,
//    isExpanded: Boolean,
//    isEditMode: Boolean,
//    onExpand: () -> Unit, // This will be used for expansion
//    onDeleteUnit: () -> Unit,
//    onDeleteSubTopic: (SubTopic) -> Unit,
//    onEditSubTopic: (SubTopic) -> Unit
//) {
//    val rotationAngle by animateFloatAsState(
//        targetValue = if (isExpanded) 180f else 0f,
//        animationSpec = tween(durationMillis = 300)
//    )
//
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth().padding(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        unit.title,
//                        color = AppColors.textPrimary,
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        modifier = Modifier.weight(1f)
//                    )
//                    if (isEditMode) {
//                        IconButton(onClick = onDeleteUnit) {
//                            Icon(Icons.Default.Delete, contentDescription = "Delete Unit")
//                        }
//                    }
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowDown,
//                        contentDescription = "Expand",
//                        modifier = Modifier
//                            .rotate(rotationAngle)
//                            .clickable { onExpand() }, // Use onExpand here to trigger expansion/collapse
//                        tint = AppColors.textSecondary
//                    )
//                }
//            }
//        }
//
//        // Only show sub-topics if unit is expanded
//        AnimatedVisibility(visible = isExpanded) {
//            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
//                HorizontalDivider(color = AppColors.background)
//                Spacer(modifier = Modifier.height(16.dp))
//                unit.subTopics.forEach { subTopic ->
//                    SubTopicItem(
//                        subTopic = subTopic,
//                        isEditMode = isEditMode,
//                        onDelete = { onDeleteSubTopic(subTopic) },
//                        onEdit = { onEditSubTopic(subTopic) }
//                    )
//                }
//            }
//        }
//    }
//}
// WORKING UNIT CARD
//@Composable
//fun UnitCard(
//    unit: UnitItem,
//    isExpanded: Boolean,
//    isEditMode: Boolean,
//    onExpand: () -> Unit,
//    onEditSubTopic: (SubTopic) -> Unit,
//    onDeleteSubTopic: (SubTopic) -> Unit,
//    onDeleteUnit: () -> Unit
//) {
//    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column {
//            Row(
//                modifier = Modifier
//                    .clickable(onClick = onExpand)
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        unit.title,
//                        fontWeight = FontWeight.Bold,
//                        color = AppColors.textPrimary,
//                        fontSize = 18.sp
//                    )
//                }
//                if (isEditMode) {
//                    // Delete Unit Button
//                    IconButton(onClick = onDeleteUnit, modifier = Modifier.size(24.dp)) {
//                        Icon(Icons.Default.Delete, contentDescription = "Delete Unit", tint = AppColors.textSecondary)
//                    }
//                }
//                Icon(
//                    imageVector = Icons.Default.KeyboardArrowDown,
//                    contentDescription = "Expand",
//                    tint = AppColors.textSecondary,
//                    modifier = Modifier.rotate(rotationAngle)
//                )
//            }
//
//            AnimatedVisibility(visible = isExpanded) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    unit.subTopics.forEach { subTopic ->
//                        SubTopicItem(
//                            subTopic = subTopic,
//                            isEditMode = isEditMode,
//                            onEdit = { onEditSubTopic(subTopic) },
//                            onDelete = { onDeleteSubTopic(subTopic) }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun SubTopicItem(subTopic: SubTopic, isEditMode: Boolean, onDelete: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            subTopic.title,
            color = AppColors.textPrimary,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        if (isEditMode) {
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Sub-topic", tint = AppColors.textSecondary)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Sub-topic", tint = AppColors.textSecondary)
            }
        }
    }
}

@Composable
fun EditSubTopicDialog(
    subTopic: SubTopic,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
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
            Button(onClick = {
                // Save the new text and close the dialog
                onSave(text)
                onDismiss() // Close the dialog after saving
            }) { Text("Save") }
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

@Preview(showBackground = true)
@Composable
fun CourseOutlineScreenPreview() {
    // Fake data setup
    val fakeUnits = listOf(
        UnitItem(
            id = 1,
            title = "Introduction",
            lessonCount = 3,
            subTopics = listOf(
                SubTopic(1, "Overview of ML"),
                SubTopic(2, "Types of ML"),
                SubTopic(3, "Applications")
            )
        )
    )

    MaterialTheme {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                CourseHeader(
                    title = "Machine Learning 101",
                    description = "A course on fundamentals of ML."
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(fakeUnits) { unit ->
                UnitCard(
                    unit = unit,
                    isExpanded = true,
                    isEditMode = false,
                    onExpand = {},
                    onEditSubTopic = {},
                    onDeleteSubTopic = {},
                    onDeleteUnit = {},
                    onAddSubTopic = {}
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

