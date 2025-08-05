package com.example.jetpackdemo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onSkip = {
                navController.navigate("home") {
                    popUpTo("welcome") { inclusive = true } // Clears back stack
                }
            })
        }
        composable("home") {
            HomeScreen()
        }
    }
}
