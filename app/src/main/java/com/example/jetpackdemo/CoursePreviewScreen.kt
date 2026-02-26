package com.example.jetpackdemo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jetpackdemo.data.model.CourseFullResponse
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePreviewScreen(
    courseId: String,
    navController: NavController,
    courseViewModel: CourseViewModel,
    onEnroll: () -> Unit,
    onStartInteractiveLearning: (firstSubtopicId: String, allSubtopicIds: List<String>) -> Unit
) {
    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()
    val enrolledCoursesState by courseViewModel.enrolledCourses.observeAsState(Resource.Loading())
    val currentUserId = courseViewModel.currentUserId

    LaunchedEffect(courseId) {
        courseViewModel.getFullCourseContent(courseId)
        courseViewModel.getEnrolledCourses()
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Course Preview", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when (val state = fullCourseContent) {
            null, is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(state.message ?: "Failed to load course", color = AppColors.textSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { courseViewModel.getFullCourseContent(courseId) },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                        ) { Text("Retry") }
                    }
                }
            }
            is Resource.Success -> {
                val data = state.data ?: return@Scaffold
                val isOwnCourse = currentUserId != null && data.course.createdBy == currentUserId
                val enrolledCourseIds = (enrolledCoursesState as? Resource.Success)?.data?.courses
                    ?.map { it.id }
                    ?.toSet()
                    ?: emptySet()
                val isEnrolledCourse = enrolledCourseIds.contains(courseId)
                val hasLearningAccess = isOwnCourse || isEnrolledCourse

                CoursePreviewContent(
                    data = data,
                    onEnroll = onEnroll,
                    onStartInteractiveLearning = onStartInteractiveLearning,
                    hasLearningAccess = hasLearningAccess,
                    isOwnCourse = isOwnCourse,
                    onGoToCourse = { navController.navigate("course_content/$courseId") },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun CoursePreviewContent(
    data: CourseFullResponse,
    onEnroll: () -> Unit,
    onStartInteractiveLearning: (String, List<String>) -> Unit,
    hasLearningAccess: Boolean = false,
    isOwnCourse: Boolean = false,
    onGoToCourse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val course = data.course
    val allSubtopics = data.units.flatMap { it.subtopics }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Course info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                if (hasLearningAccess) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.accent.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            if (isOwnCourse) "Your Course" else "Enrolled",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = AppColors.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    course.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = AppColors.textPrimary
                )
                Spacer(Modifier.height(8.dp))
                course.description?.let {
                    Text(it, color = AppColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp)
                    Spacer(Modifier.height(12.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    course.difficulty?.let { diff ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                diff.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = AppColors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.progressGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "${data.units.size} Units • ${allSubtopics.size} Topics",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = AppColors.progressGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Units & subtopics
        Text("Course Outline", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AppColors.textPrimary)
        Spacer(Modifier.height(12.dp))

        data.units.forEach { unit ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.surface)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(unit.title, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary, fontSize = 15.sp)
                    Spacer(Modifier.height(6.dp))
                    unit.subtopics.forEach { sub ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Circle, null, tint = AppColors.primary.copy(alpha = 0.4f), modifier = Modifier.size(8.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(sub.title, color = AppColors.textSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons – different for own course vs other's course
        if (hasLearningAccess) {
            Button(
                onClick = onGoToCourse,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Go to Course", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))

            if (allSubtopics.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        val firstId = allSubtopics.first().id
                        val allIds = allSubtopics.map { it.id }
                        onStartInteractiveLearning(firstId, allIds)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Start Interactive Learning", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Button(
                onClick = onEnroll,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Icon(Icons.Default.School, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Enroll in Course", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))

            if (allSubtopics.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        val firstId = allSubtopics.first().id
                        val allIds = allSubtopics.map { it.id }
                        onStartInteractiveLearning(firstId, allIds)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Start Interactive Learning", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
