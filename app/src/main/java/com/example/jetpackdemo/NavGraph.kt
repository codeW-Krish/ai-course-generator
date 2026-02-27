package com.example.jetpackdemo

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jetpackdemo.data.api.RetrofitClient
import com.example.jetpackdemo.data.repository.AudioRepository
import com.example.jetpackdemo.data.repository.CourseRepository
import com.example.jetpackdemo.data.repository.FlashcardRepository
import com.example.jetpackdemo.data.repository.NotesRepository
import com.example.jetpackdemo.data.repository.UserRepository
import com.example.jetpackdemo.shared_pref.UserPreferencesManager
import com.example.jetpackdemo.utils.TokenManager
import com.example.jetpackdemo.viewmodels.AdminViewModel
import com.example.jetpackdemo.viewmodels.AdminViewModelFactory
import com.example.jetpackdemo.viewmodels.AudioViewModel
import com.example.jetpackdemo.viewmodels.AudioViewModelFactory
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.CourseViewModelFactory
import com.example.jetpackdemo.viewmodels.FlashcardViewModel
import com.example.jetpackdemo.viewmodels.FlashcardViewModelFactory
import com.example.jetpackdemo.viewmodels.InteractiveViewModelFactory
import com.example.jetpackdemo.viewmodels.NotesViewModel
import com.example.jetpackdemo.viewmodels.NotesViewModelFactory
import com.example.jetpackdemo.viewmodels.UserViewModel
import com.example.jetpackdemo.viewmodels.UserViewModelFactory



@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current.applicationContext
    val tokenManager = TokenManager(context)
    val userPrefsManager = UserPreferencesManager(context)

    // Check if user is logged in
    val hasValidTokens = remember {
        derivedStateOf {
            val idToken = tokenManager.getIdToken() ?: tokenManager.getAccessToken()
            !idToken.isNullOrEmpty()
        }
    }.value

    // Get user role from saved preferences
    val userRole = remember(hasValidTokens) {
        if (hasValidTokens) {
            userPrefsManager.getUserRole() ?: "user"
        } else {
            "guest"
        }
    }

    // Dynamic start destination
    val startDestination = if (hasValidTokens) "main" else "welcome"

    // Shared API & Repository
    val api = RetrofitClient.getAuthApi(context)
    val repository = CourseRepository(api)
    val factory = CourseViewModelFactory(repository, context as Application)
    val courseViewModel: CourseViewModel = viewModel(factory = factory)

    // User repository & ViewModel
    val userRepository = UserRepository(api)
    val userFactory = UserViewModelFactory(userRepository, context as Application)
    val userViewModel: UserViewModel = viewModel(factory = userFactory)

    NavHost(navController = navController, startDestination = startDestination) {
        // === AUTH FLOW ===
        composable("welcome") {
            WelcomeScreen(
                onGetStarted = { navController.navigate("signup") },
                onLogin = { navController.navigate("login") }
            )
        }
        composable("signup") {
            SignUpScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController, courseViewModel = courseViewModel)
        }

        // === MAIN APP ===
        composable("main") {
            LaunchedEffect(Unit) {
                courseViewModel.reloadUserData()  // ← THIS LINE FIXES IT
            }

            MainScreen(
                navController = navController,
                courseViewModel = courseViewModel,
                adminViewModel = if (userRole == "admin") {
                    val adminFactory = AdminViewModelFactory(repository, context as Application)
                    viewModel(factory = adminFactory)
                } else null,
                userViewModel = userViewModel
            )
        }

        // === COURSE CREATION ===
        composable("create_course") {
            CreateCourseScreen(
                courseViewModel = courseViewModel,
                onNavigateBack = {
                    // Clear state when navigating back to prevent stale data
                    courseViewModel.clearOutlineState()
                    navController.popBackStack()
                },
                onGenerateOutline = {
                    courseViewModel.generateCourseOutline()
                    navController.navigate("course_outline")
                }
            )
        }
        composable("course_outline") {
            CourseOutlineScreen(
                courseViewModel = courseViewModel,
                onNavigateBack = {
                    // Clear state when navigating back
                    courseViewModel.clearOutlineState()
                    navController.popBackStack()
                },
                onGenerateContent = {
                    navController.navigate("course_content")
                },
                onStartInteractiveLearning = { cId ->
                    // Navigate to course preview which will handle interactive start
                    // Or go directly to interactive learning with the course ID
                    navController.navigate("interactive_course/$cId")
                }
            )
        }
        composable("course_content") {
            CourseContentScreen(
                courseViewModel = courseViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // === INTERACTIVE LEARNING MODE ===
        
        // Demo mode (uses dummy data)
        composable("interactive_demo") {
            val interactiveFactory = InteractiveViewModelFactory(context as Application)
            InteractiveLearningScreen(
                navController = navController,
                viewModel = viewModel(factory = interactiveFactory)
            )
        }

        // Sequential learning by course ID (main flow)
        composable("interactive_course/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val interactiveFactory = InteractiveViewModelFactory(context as Application)
            InteractiveLearningScreen(
                navController = navController,
                viewModel = viewModel(factory = interactiveFactory),
                courseId = courseId
            )
        }

        // Direct subtopic access (for resuming specific subtopic)
        composable("interactive/{subtopicId}") { backStackEntry ->
            val subtopicId = backStackEntry.arguments?.getString("subtopicId") ?: return@composable
            val interactiveFactory = InteractiveViewModelFactory(context as Application)
            InteractiveLearningScreen(
                navController = navController,
                viewModel = viewModel(factory = interactiveFactory),
                subtopicId = subtopicId
            )
        }

        // === INTERACTIVE HUB ===
        composable("interactive_hub/{courseId}/{subtopicId}/{title}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val subtopicId = backStackEntry.arguments?.getString("subtopicId") ?: return@composable
            val title = backStackEntry.arguments?.getString("title")
            InteractiveHubScreen(
                navController = navController,
                courseId = courseId,
                subtopicId = subtopicId,
                subtopicTitle = title
            )
        }

        // === FLASHCARDS ===
        composable("flashcards/{subtopicId}/{title}") { backStackEntry ->
            val subtopicId = backStackEntry.arguments?.getString("subtopicId") ?: return@composable
            val title = backStackEntry.arguments?.getString("title")
            val flashcardRepo = FlashcardRepository(api)
            val flashcardFactory = FlashcardViewModelFactory(flashcardRepo, context as Application)
            val flashcardVm: FlashcardViewModel = viewModel(factory = flashcardFactory)

            LaunchedEffect(subtopicId) { flashcardVm.loadFlashcards(subtopicId) }

            FlashcardScreen(
                navController = navController,
                viewModel = flashcardVm,
                subtopicId = subtopicId,
                subtopicTitle = title
            )
        }

        // === SUMMARY NOTES ===
        composable("notes/{subtopicId}/{title}") { backStackEntry ->
            val subtopicId = backStackEntry.arguments?.getString("subtopicId") ?: return@composable
            val title = backStackEntry.arguments?.getString("title")
            val notesRepo = NotesRepository(api)
            val notesFactory = NotesViewModelFactory(notesRepo, context as Application)
            val notesVm: NotesViewModel = viewModel(factory = notesFactory)

            LaunchedEffect(subtopicId) { notesVm.loadNotes(subtopicId) }

            SummaryNotesScreen(
                navController = navController,
                viewModel = notesVm,
                subtopicId = subtopicId,
                subtopicTitle = title
            )
        }

        // === AUDIO OVERVIEW (subtopic) ===
        composable("audio_subtopic/{subtopicId}/{title}") { backStackEntry ->
            val subtopicId = backStackEntry.arguments?.getString("subtopicId") ?: return@composable
            val title = backStackEntry.arguments?.getString("title")
            val audioRepo = AudioRepository(api)
            val audioFactory = AudioViewModelFactory(audioRepo, context as Application)
            val audioVm: AudioViewModel = viewModel(factory = audioFactory)

            LaunchedEffect(subtopicId) { audioVm.loadSubtopicAudio(subtopicId) }

            AudioOverviewScreen(
                navController = navController,
                viewModel = audioVm,
                subtopicId = subtopicId,
                title = title
            )
        }

        // === VIDEO PLAYER (subtopic) ===
        composable("video_player/{subtopicId}/{title}") { backStackEntry ->
            val subtopicId = backStackEntry.arguments?.getString("subtopicId") ?: return@composable
            val title = backStackEntry.arguments?.getString("title")
            val videoRepo = com.example.jetpackdemo.data.repository.VideoRepository(api)
            val videoFactory = com.example.jetpackdemo.viewmodels.VideoViewModelFactory(videoRepo, context as Application)
            val videoVm: com.example.jetpackdemo.viewmodels.VideoViewModel = viewModel(factory = videoFactory)

            VideoPlayerScreen(
                navController = navController,
                viewModel = videoVm,
                subtopicId = subtopicId,
                title = title
            )
        }

        // === AUDIO OVERVIEW (course) ===
        composable("audio_course/{courseId}/{title}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val title = backStackEntry.arguments?.getString("title")
            val audioRepo = AudioRepository(api)
            val audioFactory = AudioViewModelFactory(audioRepo, context as Application)
            val audioVm: AudioViewModel = viewModel(factory = audioFactory)

            LaunchedEffect(courseId) { audioVm.loadCourseAudio(courseId) }

            AudioOverviewScreen(
                navController = navController,
                viewModel = audioVm,
                courseId = courseId,
                title = title
            )
        }

        // === AI TUTOR CHAT ===
        composable("tutor_chat/{subtopicId}/{title}") { backStackEntry ->
            val subtopicId = backStackEntry.arguments?.getString("subtopicId") ?: return@composable
            val title = backStackEntry.arguments?.getString("title")
            TutorChatScreen(
                navController = navController,
                subtopicId = subtopicId,
                subtopicTitle = title
            )
        }

        // === ADMIN ROUTES (Only if logged in + admin) ===
        if (hasValidTokens && userRole == "admin") {
            composable("adminCourses") {
                val adminFactory = AdminViewModelFactory(repository, context as Application)
                val adminViewModel: AdminViewModel = viewModel(factory = adminFactory)
                AdminCoursesScreen(navController = navController, adminViewModel = adminViewModel)
            }

            composable("adminSettings") {
                val adminFactory = AdminViewModelFactory(repository, context as Application)
                val adminViewModel: AdminViewModel = viewModel(factory = adminFactory)
                AdminSettingsScreen(navController = navController, adminViewModel = adminViewModel)
            }
        }
    }
}
