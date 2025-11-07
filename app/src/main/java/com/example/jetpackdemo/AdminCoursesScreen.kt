package com.example.jetpackdemo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.jetpackdemo.data.model.Course
import com.example.jetpackdemo.data.model.CoursesResponse
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.AdminViewModel
import com.example.jetpackdemo.viewmodels.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCoursesScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel
) {
    // === LOAD COURSES ON APPEAR ===
    LaunchedEffect(Unit) {
        adminViewModel.loadAllCoursesAdmin()
    }

    // === OBSERVE STATE ===
    val adminCoursesState by adminViewModel.adminCourses.collectAsState()

    // === EXTRACT DATA ===
    val courses = (adminCoursesState as? Resource.Success<CoursesResponse>)?.data?.courses ?: emptyList()
    val isLoading = adminCoursesState is Resource.Loading
    val error = (adminCoursesState as? Resource.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin - All Courses", color = AppColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = Color.Red)
                }
            }
            courses.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No courses found", color = AppColors.textSecondary)
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(courses) { course ->
                        AdminCourseCard(
                            course = course,
                            onDelete = { adminViewModel.deleteCourse(course.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCourseCard(
    course: Course,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AppColors.textPrimary
            )
            Text(
                text = "By: ${course.createdBy}",
                fontSize = 14.sp,
                color = AppColors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}