// EnrolledCoursesScreen.kt
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.data.model.Course
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource
import com.example.jetpackdemo.data.model.enrolledCoursesResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrolledCoursesScreen(
    viewModel: CourseViewModel,
    onCourseClick: (String) -> Unit
) {
    val enrolledState by viewModel.enrolledCourses.observeAsState(
         Resource.Loading<enrolledCoursesResponse>()
    )

    LaunchedEffect(Unit) { viewModel.getEnrolledCourses() }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Enrolled Courses",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        when (val state = enrolledState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }
            is Resource.Success -> {
                val courses: List<Course> = state.data?.courses ?: emptyList()

                if (courses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppColors.textSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No enrolled courses",
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Browse and enroll in public courses",
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
                            EnrolledCourseCard(
                                course = course,
                                onClick = {
                                    viewModel.setCourseId(course.id)
                                    onCourseClick(course.id)
                                }
                            )
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                        Text("Failed to load enrolled courses", color = AppColors.textSecondary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ENROLLED COURSE CARD – CLEAN UI
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EnrolledCourseCard(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Course icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.progressGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = AppColors.progressGreen,
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    course.difficulty?.let { diff ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (diff.lowercase()) {
                                "beginner" -> Color(0xFF10B981).copy(alpha = 0.1f)
                                "intermediate" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                "advanced" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                else -> AppColors.primary.copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                diff.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
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

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AppColors.progressGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "Enrolled",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = AppColors.progressGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Chevron arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = AppColors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}