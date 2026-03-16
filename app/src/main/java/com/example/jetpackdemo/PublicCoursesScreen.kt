package com.example.jetpackdemo

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.data.model.Course
import com.example.jetpackdemo.data.model.CoursesResponse
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicCoursesScreen(
    viewModel: CourseViewModel,
    onJoinCourse: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onCoursePreviewClicked: (String) -> Unit = {},
    onCreatorClick: (String) -> Unit = {}
) {
    val publicCoursesState by viewModel.publicCourses.observeAsState(Resource.Loading<CoursesResponse>())
    val currentUserId = viewModel.currentUserId
    val enrollResult by viewModel.enrollResult.observeAsState(Resource.Loading())
    var enrollingCourseId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val difficulties = listOf("All", "Beginner", "Intermediate", "Advanced")
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(enrollResult) {
        when (enrollResult) {
            is Resource.Success -> {
                Toast.makeText(context, "Successfully enrolled!", Toast.LENGTH_SHORT).show()
                enrollingCourseId = null
                viewModel.clearEnrollResult()
            }
            is Resource.Error -> {
                Toast.makeText(context, enrollResult.message ?: "Failed to enroll", Toast.LENGTH_SHORT).show()
                enrollingCourseId = null
                viewModel.clearEnrollResult()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) { viewModel.getAllPublicCourses() }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Public Courses", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(difficulties) { difficulty ->
                    FilterChip(
                        selected = selectedFilter == difficulty,
                        onClick = { selectedFilter = difficulty },
                        label = { Text(difficulty, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.primary,
                            selectedLabelColor = AppColors.onPrimary,
                            containerColor = AppColors.surface,
                            labelColor = AppColors.textSecondary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            when (publicCoursesState) {
                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.primary)
                    }
                }
                is Resource.Success -> {
                    val allCourses = publicCoursesState.data?.courses
                        ?.filter { it.createdBy != currentUserId } ?: emptyList()
                    val courses = if (selectedFilter == "All") allCourses
                    else allCourses.filter { it.difficulty?.lowercase() == selectedFilter.lowercase() }

                    if (courses.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.People,
                                    null,
                                    tint = AppColors.textSecondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    if (selectedFilter == "All") "No public courses available"
                                    else "No $selectedFilter courses available",
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(courses) { course ->
                                PublicCourseCard(
                                    course = course,
                                    isEnrolling = enrollingCourseId == course.id && enrollResult is Resource.Loading,
                                    onJoin = {
                                        enrollingCourseId = course.id
                                        onJoinCourse(course.id)
                                    },
                                    onPreview = { onCoursePreviewClicked(course.id) },
                                    onCreatorClick = {
                                        course.createdBy?.let { onCreatorClick(it) }
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error loading courses", color = Color.Red, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(publicCoursesState.message ?: "", color = AppColors.textSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicCourseCard(
    course: Course,
    isEnrolling: Boolean = false,
    onJoin: () -> Unit,
    onPreview: () -> Unit = {},
    onCreatorClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: difficulty badge + enrolled count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                } ?: Spacer(Modifier)

                if (course.totalUsersJoined > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${course.totalUsersJoined} enrolled",
                            fontSize = 12.sp,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                course.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                course.description ?: "No description available",
                fontSize = 13.sp,
                color = AppColors.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp
            )

            // Creator row
            course.creatorName?.let { name ->
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.clickable(onClick = onCreatorClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = AppColors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        name,
                        fontSize = 13.sp,
                        color = AppColors.accent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Buttons row: Preview + Enroll
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.primary)
                ) {
                    Text("Preview", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onJoin,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                    enabled = !isEnrolling
                ) {
                    if (isEnrolling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AppColors.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Enrolling...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Enroll", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.onPrimary)
                    }
                }
            }
        }
    }
}