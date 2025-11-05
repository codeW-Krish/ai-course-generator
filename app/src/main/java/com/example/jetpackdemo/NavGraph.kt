package com.example.jetpackdemo

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jetpackdemo.data.api.RetrofitClient
import com.example.jetpackdemo.data.repository.CourseRepository
import com.example.jetpackdemo.ui.viewmodel.CourseViewModel
import com.example.jetpackdemo.viewmodels.CourseViewModelFactory

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current.applicationContext  // APPLICATION CONTEXT

    // ⚠️ Do NOT use remember here
    val api = RetrofitClient.getAuthApi(context)
    val repository = CourseRepository(api)
    val factory = CourseViewModelFactory(repository, context as Application)

    // ✅ ViewModel scoped properly
    val courseViewModel: CourseViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "welcome") {
        // --- Authentication Flow ---
        composable("welcome") {
            WelcomeScreen(
                onGetStarted = { navController.navigate("signup") },
                onLogin = { navController.navigate("login") }
            )
        }
        composable("signup") {
            // Updated to pass the NavController
            SignUpScreen(navController = navController)
        }
        composable("login") {
            // Updated to pass the NavController
            LoginScreen(navController = navController)
        }


        // --- Main App Screen (with bottom navigation) ---
        composable("main") {
            // Pass the main NavController to the MainScreen
            MainScreen(navController = navController)
        }

        // --- Course Creation Flow (navigated to from Home) ---
//        composable("create_course") {
//            CreateCourseScreen(
//                onNavigateBack = { navController.popBackStack() },
//                onGenerateOutline = {
//                    navController.navigate("course_outline")
//                }
//            )
//        }
        composable("create_course") {
            CreateCourseScreen(
                courseViewModel = courseViewModel,
                onNavigateBack = { navController.popBackStack() },
                onGenerateOutline = {
                    courseViewModel.generateCourseOutline()
                    navController.navigate("course_outline")
                }
            )
        }
        composable("course_outline") {
            CourseOutlineScreen(
                courseViewModel = courseViewModel,
                onNavigateBack = { navController.popBackStack() },
                onGenerateContent = {
                    navController.navigate("course_content")
                }
            )
        }
        composable("course_content") {
            CourseContentScreen(
                courseViewModel = courseViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
