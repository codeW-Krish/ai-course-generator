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
import com.example.jetpackdemo.viewmodels.UserViewModel

// --- Data Models ---
data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)

// --- Main Screen with Bottom Navigation ---
@Composable
fun MainScreen(navController: NavHostController, courseViewModel: CourseViewModel, adminViewModel: AdminViewModel? = null, userViewModel: UserViewModel? = null) {
    val bottomBarNavController = rememberNavController()
    Scaffold(
        bottomBar = { HomeBottomNavigation(navController = bottomBarNavController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            BottomNavGraph(
                bottomBarNavController = bottomBarNavController,
                appNavController = navController,
                courseViewModel = courseViewModel,
                adminViewModel = adminViewModel,
                userViewModel = userViewModel
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
    adminViewModel: AdminViewModel? = null,
    userViewModel: UserViewModel? = null
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
                    bottomBarNavController.navigate("course_preview/$courseId")
                }
            )
        }

        // Inside BottomNavGraph
        composable("enrolled_courses") {
            EnrolledCoursesScreen(
                viewModel = courseViewModel,
                onCourseClick = { courseId ->
                    bottomBarNavController.navigate("course_preview/$courseId")
                }
            )
        }

        composable("public_courses") {
            PublicCoursesScreen(
                viewModel = courseViewModel,
                onJoinCourse = { courseId ->
                    courseViewModel.enrollInCourse(courseId)
                },
                onNavigateBack = { bottomBarNavController.popBackStack() },
                onCoursePreviewClicked = { courseId ->
                    bottomBarNavController.navigate("course_preview/$courseId")
                },
                onCreatorClick = { creatorId ->
                    bottomBarNavController.navigate("creator_profile/$creatorId")
                }
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
                navController = bottomBarNavController,
                appNavController = appNavController,
                courseViewModel = courseViewModel,
                adminViewModel = adminViewModel,
                userViewModel = userViewModel
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
                onStartInteractiveLearning = { _, allSubtopicIds ->
                    // Resume via course route so backend returns next unfinished subtopic
                    courseViewModel.setInteractiveSubtopics(allSubtopicIds)
                    appNavController.navigate("interactive_course/$courseId")
                }
            )
        }

        // Creator Profile Screen
        composable("creator_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            if (userViewModel != null) {
                CreatorProfileScreen(
                    userId = userId,
                    userViewModel = userViewModel,
                    onNavigateBack = { bottomBarNavController.popBackStack() },
                    onCourseClick = { courseId ->
                        bottomBarNavController.navigate("course_preview/$courseId")
                    },
                    onFollowersClick = { uid ->
                        bottomBarNavController.navigate("followers_list/$uid")
                    },
                    onFollowingClick = { uid ->
                        bottomBarNavController.navigate("following_list/$uid")
                    }
                )
            }
        }

        // Followers List
        composable("followers_list/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            if (userViewModel != null) {
                FollowListScreen(
                    userId = userId,
                    isFollowers = true,
                    userViewModel = userViewModel,
                    onNavigateBack = { bottomBarNavController.popBackStack() },
                    onUserClick = { uid -> bottomBarNavController.navigate("creator_profile/$uid") }
                )
            }
        }

        // Following List
        composable("following_list/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            if (userViewModel != null) {
                FollowListScreen(
                    userId = userId,
                    isFollowers = false,
                    userViewModel = userViewModel,
                    onNavigateBack = { bottomBarNavController.popBackStack() },
                    onUserClick = { uid -> bottomBarNavController.navigate("creator_profile/$uid") }
                )
            }
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
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchResults by courseViewModel.fullSearchResults.collectAsStateWithLifecycle()

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
            if (!isSearchActive) {
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
            item { Spacer(Modifier.height(4.dp)) }

            // === SEARCH BAR ===
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        isSearchActive = query.isNotBlank()
                        if (query.length >= 2) {
                            courseViewModel.searchCoursesFull(query, null, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search courses...", color = AppColors.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = AppColors.textSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(Icons.Default.Close, "Clear", tint = AppColors.textSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.textSecondary.copy(alpha = 0.2f),
                        focusedContainerColor = AppColors.surface,
                        unfocusedContainerColor = AppColors.surface,
                        cursorColor = AppColors.primary
                    ),
                    singleLine = true
                )
            }

            if (isSearchActive && searchQuery.length >= 2) {
                // === SEARCH RESULTS ===
                if (searchResults.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, null, tint = AppColors.textSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No courses found for \"$searchQuery\"", color = AppColors.textSecondary, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    items(searchResults) { result ->
                        SearchResultCard(
                            result = result,
                            onClick = { onCoursePreviewClicked(result.id) }
                        )
                    }
                }
            } else {
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
}

@Composable
fun SearchResultCard(
    result: com.example.jetpackdemo.data.model.FullSearchItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AppColors.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.School, null, tint = AppColors.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    result.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = AppColors.textPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    result.creator_name?.let { name ->
                        Text("by $name", fontSize = 12.sp, color = AppColors.textSecondary)
                    }
                    result.difficulty?.let { diff ->
                        Text(
                            "• ${diff.replaceFirstChar { it.uppercase() }}",
                            fontSize = 12.sp,
                            color = when (diff.lowercase()) {
                                "beginner" -> Color(0xFF10B981)
                                "intermediate" -> Color(0xFFF59E0B)
                                "advanced" -> Color(0xFFEF4444)
                                else -> AppColors.textSecondary
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (result.total_users_joined > 0) {
                        Text(
                            "• ${result.total_users_joined} enrolled",
                            fontSize = 12.sp,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = AppColors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
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
    val currentUserId = courseViewModel.currentUserId
    val enrollResult by courseViewModel.enrollResult.observeAsState(Resource.Loading())
    val isEnrolling = enrollResult is Resource.Loading && enrollResult.data == null &&
            enrollResult.message == null // initial state is also Loading, so track separately
    var enrollingCourseId by remember { mutableStateOf<String?>(null) }

    Column {
        SectionHeader(title = "Discover Courses", onSeeAll = onSeeAllClicked)

        when (publicCoursesState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }
            is Resource.Success -> {
                val courses = publicCoursesState.data?.courses
                    ?.filter { it.createdBy != currentUserId }
                    ?.take(5) ?: emptyList()
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
                                onJoinClick = {
                                    enrollingCourseId = course.id
                                    courseViewModel.enrollInCourse(course.id)
                                },
                                isEnrolling = enrollingCourseId == course.id && enrollResult is Resource.Loading
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