package com.example.jetpackdemo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jetpackdemo.data.model.Course
import com.example.jetpackdemo.data.model.CoursesResponse
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.AdminViewModel
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource

// --- Data Models ---
data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)

// --- Main Screen with Bottom Navigation ---
@Composable
fun MainScreen(navController: NavHostController, courseViewModel: CourseViewModel, adminViewModel: AdminViewModel? = null) {
    val bottomBarNavController = rememberNavController()
    Scaffold(
        bottomBar = { HomeBottomNavigation(navController = bottomBarNavController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            BottomNavGraph(
                bottomBarNavController = bottomBarNavController,
                appNavController = navController,
                courseViewModel = courseViewModel,
                adminViewModel = adminViewModel
            )
        }
    }
}

// --- Navigation Graph for the Bottom Bar Tabs ---
@Composable
fun BottomNavGraph(
    bottomBarNavController: NavHostController,
    appNavController: NavHostController,
    courseViewModel: CourseViewModel,
    adminViewModel: AdminViewModel? = null
) {
    NavHost(navController = bottomBarNavController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onCreateCourseClicked = { appNavController.navigate("create_course") },
                onInteractiveDemoClicked = { appNavController.navigate("interactive_demo") },
                onCoursePreviewClicked = { courseId -> bottomBarNavController.navigate("course_preview/$courseId") },
                courseViewModel = courseViewModel,
                onSeeAllPublic = { bottomBarNavController.navigate("public_courses") }
            )
        }
        composable("my_courses") {
            MyCoursesScreen(
                viewModel = courseViewModel,
                onCourseClick = { courseId ->
                    bottomBarNavController.navigate("course_content/$courseId")
                }
            )
        }

        // Inside BottomNavGraph
        composable("enrolled_courses") {
            EnrolledCoursesScreen(
                viewModel = courseViewModel,
                onCourseClick = { courseId ->
                    bottomBarNavController.navigate("course_content/$courseId")
                }
            )
        }

        composable("public_courses") {
            PublicCoursesScreen(
                viewModel = courseViewModel,
                onJoinCourse = { courseId ->
                    courseViewModel.enrollInCourse(courseId)
                },
                onNavigateBack = { bottomBarNavController.popBackStack() }
            )
        }

        composable("course_content/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            if (courseId.isBlank()) return@composable

            LaunchedEffect(courseId) {
                courseViewModel.setCourseId(courseId)
            }

            CourseContentScreen(
                courseViewModel = courseViewModel,
                onNavigateBack = { bottomBarNavController.popBackStack() }
            )
        }
        composable("profile") {
            UserProfileScreen(
                navController = appNavController,
                courseViewModel = courseViewModel,
                adminViewModel = adminViewModel
            )
        }

        // Course Preview Screen
        composable("course_preview/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            CoursePreviewScreen(
                courseId = courseId,
                navController = bottomBarNavController,
                courseViewModel = courseViewModel,
                onEnroll = { courseViewModel.enrollInCourse(courseId) },
                onStartInteractiveLearning = { firstSubtopicId, allSubtopicIds ->
                    // Navigate to interactive learning with first subtopic
                    // Pass all IDs via saved state or ViewModel for sequential navigation
                    courseViewModel.setInteractiveSubtopics(allSubtopicIds)
                    appNavController.navigate("interactive/$firstSubtopicId")
                }
            )
        }
    }
}

// --- HomeScreen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateCourseClicked: () -> Unit,
    onInteractiveDemoClicked: () -> Unit,
    onCoursePreviewClicked: (String) -> Unit,
    courseViewModel: CourseViewModel,
    onSeeAllPublic: () -> Unit
) {

    val context = LocalContext.current
    val enrollResult by courseViewModel.enrollResult.observeAsState(Resource.Loading())
    LaunchedEffect(enrollResult) {
        when (enrollResult) {
            is Resource.Success -> {
                Toast.makeText(context, "Successfully enrolled!", Toast.LENGTH_SHORT).show()
                courseViewModel.clearEnrollResult()
            }
            is Resource.Error -> {
                Toast.makeText(context, enrollResult.message ?: "Failed to enroll", Toast.LENGTH_SHORT).show()
                courseViewModel.clearEnrollResult()
            }
            else -> Unit
        }
    }



    LaunchedEffect(Unit) { courseViewModel.getAllPublicCourses() }

    Scaffold(
        containerColor = AppColors.background,
        topBar = { HomeTopBar(courseViewModel) },
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
                    Spacer(Modifier.width(8.dp))
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // === INTERACTIVE LEARNING DEMO BUTTON ===
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D6A4F)),
                    onClick = onInteractiveDemoClicked
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Try Interactive Learning", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text("Experience the new quiz-based mode", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(8.dp)) }

            item {
                DiscoverCoursesSection(
                    courseViewModel = courseViewModel,
                    onSeeAllClicked = onSeeAllPublic,
                    onCourseClicked = onCoursePreviewClicked
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DiscoverCoursesSection(
    courseViewModel: CourseViewModel,
    onSeeAllClicked: () -> Unit,
    onCourseClicked: (String) -> Unit
) {
    val publicCoursesState by courseViewModel.publicCourses.observeAsState(Resource.Loading<CoursesResponse>())

    Column {
        SectionHeader(title = "Discover Courses", onSeeAll = onSeeAllClicked)

        when (publicCoursesState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }
            is Resource.Success -> {
                val courses = publicCoursesState.data?.courses?.take(5) ?: emptyList()
                if (courses.isEmpty()) {
                    Box(modifier = Modifier.height(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No courses available yet", color = AppColors.textSecondary)
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(courses) { course ->
                            com.example.jetpackdemo.ui.components.PremiumCourseCard(
                                course = course,
                                onCardClick = { onCourseClicked(course.id) },
                                onJoinClick = { courseViewModel.enrollInCourse(course.id) }
                            )
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Error loading courses", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun DiscoverCourseCard(
    course: Course,
    onJoin: () -> Unit  // ← NEW
) {
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(course.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("ID: ${course.id}", fontSize = 14.sp, color = AppColors.textSecondary)
            Spacer(Modifier.height(4.dp))
            course.description?.let {
                Text(it, fontSize = 14.sp, color = AppColors.textSecondary, maxLines = 2)
                Spacer(Modifier.height(4.dp))
            }
            course.difficulty?.let {
                Text("Level: $it", fontSize = 14.sp, color = AppColors.textSecondary)
            }
            Spacer(Modifier.height(12.dp))

            // CONNECT TO onJoin
            OutlinedButton(
                onClick = onJoin,  // ← NOW CALLS enroll
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Join")
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.textPrimary)
        TextButton(onClick = onSeeAll) {
            Text("See all", color = AppColors.accent, fontWeight = FontWeight.Bold)
        }
    }
}

// --- Bottom Navigation ---
@Composable
fun HomeBottomNavigation(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Home", "home", Icons.Default.Home),
        BottomNavItem("My Courses", "my_courses", Icons.AutoMirrored.Filled.ShowChart),
        BottomNavItem("Enrolled", "enrolled_courses", Icons.Default.CheckCircle),
        BottomNavItem("Profile", "profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = AppColors.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.primary,
                    unselectedIconColor = AppColors.textSecondary,
                    selectedTextColor = AppColors.primary,
                    unselectedTextColor = AppColors.textSecondary,
                    indicatorColor = AppColors.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

// --- Top Bar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(courseViewModel: CourseViewModel) {
    val username by courseViewModel.username.collectAsState()
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hi", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Welcome back, $username",
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}