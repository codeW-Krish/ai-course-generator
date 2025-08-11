package com.example.jetpackdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.ui.theme.AppColors

// Data classes to represent the UI models
data class Course(
    val title: String,
    val lessonCount: Int,
    val progress: Float
)

data class DiscoverCourse(
    val title: String,
    val instructor: String,
    val studentCount: String,
    val tag: String,
    val level: String,
    val instructorImage: Int // Using drawable resource ID
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onCreateCourseClicked: () -> Unit) {
    Scaffold(
        containerColor = AppColors.background,
        topBar = { HomeTopBar() },
        bottomBar = { HomeBottomNavigation() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateCourseClicked,
                containerColor = AppColors.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Course", tint = AppColors.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Course", color = AppColors.onPrimary)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                YourCoursesSection()
                Spacer(modifier = Modifier.height(24.dp))
                DiscoverCoursesSection()
                Spacer(modifier = Modifier.height(80.dp)) // Space for the FAB
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👋", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Welcome back, Krish!",
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun YourCoursesSection() {
    val courses = listOf(
        Course("Machine Learning Basics", 12, 0.68f),
        Course("Python for Data Science", 8, 0.85f),
        Course("Advanced Android", 20, 0.30f)
    )

    Column {
        SectionHeader(title = "Your Courses")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(courses) { course ->
                CourseProgressCard(course)
            }
        }
    }
}

@Composable
fun CourseProgressCard(course: Course) {
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(course.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.textPrimary)
            Text("${course.lessonCount} lessons", fontSize = 14.sp, color = AppColors.textSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Progress", fontSize = 12.sp, color = AppColors.textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { course.progress },
                    modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                    color = AppColors.accent,
                    trackColor = AppColors.background
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${(course.progress * 100).toInt()}%", fontWeight = FontWeight.SemiBold, color = AppColors.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Handle continue */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue", color = AppColors.onPrimary)
            }
        }
    }
}

@Composable
fun DiscoverCoursesSection() {
    val discoverCourses = listOf(
        DiscoverCourse("Natural Language Processing", "Alex Morgan", "2,458 students", "AI", "Beginner", 0),
        DiscoverCourse("React.js for AI Applications", "Sarah Johnson", "1,872 students", "Web", "Intermediate", 0),
        DiscoverCourse("Computer Vision Masterclass", "Michael Chen", "3,124 students", "AI", "Advanced", 0)
    )

    Column {
        SectionHeader(title = "Discover Courses")
        discoverCourses.forEach { course ->
            DiscoverCourseCard(course)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DiscoverCourseCard(course: DiscoverCourse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(AppColors.background))

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppColors.textPrimary)
                Text(course.instructor, fontSize = 14.sp, color = AppColors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = "Students", tint = AppColors.textSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(course.studentCount, fontSize = 12.sp, color = AppColors.textSecondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(text = course.tag)
                    Chip(text = course.level)
                }
            }

            OutlinedButton(
                onClick = { /* Handle Join */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AppColors.primary))
            ) {
                Text("Join")
            }
        }
    }
}


@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text("See all", fontSize = 14.sp, color = AppColors.accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(AppColors.accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = AppColors.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HomeBottomNavigation() {
    NavigationBar(
        containerColor = AppColors.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem("Home", Icons.Default.Home),
            BottomNavItem("Progress", Icons.Default.ShowChart),
            BottomNavItem("Profile", Icons.Default.Person)
        )
        val selectedItem = items[0]

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedItem == item,
                onClick = { /* Handle navigation */ },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.primary,
                    unselectedIconColor = AppColors.textSecondary,
                    selectedTextColor = AppColors.primary,
                    unselectedTextColor = AppColors.textSecondary,
                    indicatorColor = AppColors.accent.copy(alpha = 0.15f)
                )
            )
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector)

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun HomeScreenPreview() {
    HomeScreen({})
}
