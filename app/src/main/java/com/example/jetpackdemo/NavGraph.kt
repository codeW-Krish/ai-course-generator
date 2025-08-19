package com.example.jetpackdemo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jetpackdemo.ui.theme.AppColors // Import the central theme

@Composable
fun AppNavGraph(navController: NavHostController) {
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
        composable("create_course") {
            CreateCourseScreen(
                onNavigateBack = { navController.popBackStack() },
                onGenerateOutline = {
                    navController.navigate("course_outline")
                }
            )
        }
        composable("course_outline") {
            CourseOutlineScreen(
                onNavigateBack = { navController.popBackStack() },
                onGenerateContent = {
                    navController.navigate("course_content")
                }
            )
        }
        composable("course_content") {
            CourseContentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
