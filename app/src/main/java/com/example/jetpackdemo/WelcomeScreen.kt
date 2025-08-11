package com.example.jetpackdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.ui.theme.AppColors

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    Surface(color = AppColors.surface, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section with Icon and App Name
            Spacer(modifier = Modifier.weight(0.5f))
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "AiLearner Logo",
                tint = AppColors.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "AiLearner",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.weight(1f))

            // Bottom section with headline, buttons, and legal text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .background(
                        AppColors.background,
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Learn Anything.\nFaster. Smarter.",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "AI helps you generate personalized courses in seconds.",
                        fontSize = 16.sp,
                        color = AppColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = onGetStarted, // Navigate to Sign Up
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                    ) {
                        Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.onPrimary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onLogin, // Navigate to Log In
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AppColors.primary))
                    ) {
                        Text("I Have an Account", fontSize = 18.sp, color = AppColors.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(onGetStarted = {}, onLogin = {})
}
