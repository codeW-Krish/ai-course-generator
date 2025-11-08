package com.example.jetpackdemo


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.jetpackdemo.data.model.CoursesResponse
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicCoursesScreen(
    viewModel: CourseViewModel,
    onJoinCourse: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val publicCoursesState by viewModel.publicCourses.observeAsState(Resource.Loading<CoursesResponse>())

    LaunchedEffect(Unit) { viewModel.getAllPublicCourses() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Public Courses", color = AppColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.textPrimary)
                    }
                }
            )
        }
    ) { padding ->
        when (publicCoursesState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }
            is Resource.Success -> {
                val courses = publicCoursesState.data?.courses ?: emptyList()
                LazyColumn(
                    modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(courses) { course ->
                        PublicCourseCard(
                            course = course,
                            onJoin = { onJoinCourse(course.id) }
                        )
                    }
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${publicCoursesState.message}", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun PublicCourseCard(course: Course, onJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(course.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(course.description ?: "No description", fontSize = 14.sp, color = AppColors.textSecondary)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Join Course", color = AppColors.onPrimary)
            }
        }
    }
}