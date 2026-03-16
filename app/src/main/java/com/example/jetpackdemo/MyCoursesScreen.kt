// MyCoursesScreen.kt
package com.example.jetpackdemo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.runtime.livedata.observeAsState
import com.example.jetpackdemo.data.model.Course
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    viewModel: CourseViewModel,
    onCourseClick: (String) -> Unit
) {
    val myCoursesState by viewModel.myCourses.observeAsState(Resource.Loading())
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getMyCourses()
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { Text("My Courses", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        when (val state = myCoursesState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }

            is Resource.Success -> {
                val courses = state.data?.courses ?: emptyList()

                if (courses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = AppColors.textSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No courses yet",
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Create your first course to get started",
                                color = AppColors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(courses) { course ->
                            MyGeneratedCourseCard(
                                course = course,
                                onClick = {
                                    viewModel.setCourseId(course.id)
                                    onCourseClick(course.id)
                                },
                                onDelete = {
                                    viewModel.deleteMyCourse(course.id) {
                                        Toast.makeText(context, "Course deleted!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Failed to load courses", color = AppColors.textSecondary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MY GENERATED COURSE CARD – POLISHED UI
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MyGeneratedCourseCard(
    course: Course,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Course icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = AppColors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AppColors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!course.description.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = course.description,
                            fontSize = 13.sp,
                            color = AppColors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                }

                // 3-dot menu
                var expanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = AppColors.textSecondary
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Course", color = Color(0xFFEF4444)) },
                            onClick = {
                                expanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        )
                    }
                }
            }

            // Bottom row with badges
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Difficulty badge
                course.difficulty?.let { diff ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (diff.lowercase()) {
                            "beginner" -> Color(0xFF10B981).copy(alpha = 0.1f)
                            "intermediate" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            "advanced" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                            else -> AppColors.primary.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            diff.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = when (diff.lowercase()) {
                                "beginner" -> Color(0xFF10B981)
                                "intermediate" -> Color(0xFFF59E0B)
                                "advanced" -> Color(0xFFEF4444)
                                else -> AppColors.primary
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status badge
                course.status?.let { status ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (status.lowercase()) {
                            "completed", "content_generated" -> AppColors.progressGreen.copy(alpha = 0.1f)
                            "generating", "generating_content" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            else -> AppColors.primary.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            when (status.lowercase()) {
                                "content_generated" -> "Ready"
                                "generating_content" -> "Generating..."
                                "outline_generated" -> "Outline Ready"
                                else -> status.replace("_", " ").replaceFirstChar { it.uppercase() }
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = when (status.lowercase()) {
                                "completed", "content_generated" -> AppColors.progressGreen
                                "generating", "generating_content" -> Color(0xFFF59E0B)
                                else -> AppColors.primary
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Public/Private badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (course.isPublic) AppColors.primary.copy(alpha = 0.1f) else AppColors.textSecondary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (course.isPublic) Icons.Default.Public else Icons.Default.Lock,
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = if (course.isPublic) AppColors.primary else AppColors.textSecondary
                        )
                        Text(
                            if (course.isPublic) "Public" else "Private",
                            fontSize = 11.sp,
                            color = if (course.isPublic) AppColors.primary else AppColors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}