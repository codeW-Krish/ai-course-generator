// EnrolledCoursesScreen.kt
package com.example.jetpackdemo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo. data.model.Course
import com.example.jetpackdemo.data.model.MyCoursesResponse
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jetpackdemo.data.model.CoursesResponse
import com.example.jetpackdemo.data.model.enrolledCoursesResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrolledCoursesScreen(
    viewModel: CourseViewModel,
    onCourseClick: (String) -> Unit
) {
    // Fix 1: Use collectAsStateWithLifecycle + generic type
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
                // Fix 2: Safe access + explicit type
                val courses: List<Course> = state.data?.courses ?: emptyList()


                if (courses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No enrolled courses yet", color = AppColors.textSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(courses) { course ->
                            // Fix 3: Use correct Course model (has .id)
                            MyGeneratedCourseCard(
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
                    Text(
                        "Error: ${state.message}",
                        color = Color.Red
                    )
                }
            }
        }
    }
}