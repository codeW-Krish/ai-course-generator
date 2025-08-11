package com.example.jetpackdemo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jetpackdemo.ui.theme.AppColors // Import the central theme

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onGetStarted = { navController.navigate("signup") },
                onLogin = { navController.navigate("login") }
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true } // Clear back stack to prevent going back to auth flow
                    }
                },
                onLoginClicked = { navController.navigate("login") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true } // Clear back stack
                    }
                },
                onSignUpClicked = { navController.navigate("signup") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            // Pass a lambda to handle the FAB click navigation
            HomeScreen(
                onCreateCourseClicked = { navController.navigate("create_course") }
            )
        }
        composable("create_course") {
            CreateCourseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
