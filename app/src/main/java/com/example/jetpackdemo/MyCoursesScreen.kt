package com.example.jetpackdemo
// MyCoursesScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.data.model.Course
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource
import com.example.jetpackdemo.data.model.MyCoursesResponse


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    viewModel: CourseViewModel,
    onCourseClick: (String) -> Unit
) {
    // Use observeAsState for LiveData, not collectAsState
    val myCoursesState by viewModel.myCourses.observeAsState(Resource.Loading())
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }

            is Resource.Success -> {
                val courses = state.data?.courses ?: emptyList()

                if (courses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You haven't created any courses yet", color = AppColors.textSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(courses) { course ->
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
                        color = AppColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun MyGeneratedCourseCard(
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = "Course ID: ${course.id}",
                fontSize = 14.sp,
                color = AppColors.textSecondary
            )
            Spacer(Modifier.height(4.dp))


            Text(
                text = "Description: ${course.description ?: "No description"}",
                fontSize = 14.sp,
                color = AppColors.textSecondary
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = "Created: ${course.createdAt ?: "N/A"}",
                fontSize = 14.sp,
                color = AppColors.textSecondary
            )
        }
    }
}
