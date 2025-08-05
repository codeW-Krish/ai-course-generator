package com.example.jetpackdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Reusing the modern color palette for consistency
object AppColors {
    val primary = Color(0xFF4A90E2)
    val primaryVariant = Color(0xFF3A77C8)
    val accent = Color(0xFF50E3C2)
    val background = Color(0xFFF7F9FC)
    val surface = Color.White
    val textPrimary = Color(0xFF4A4A4A)
    val textSecondary = Color(0xFF7F8C8D)
    val chipBackground = Color(0xFFE8F0FE)
    val darkButton = Color(0xFF2C3E50)
}

@Composable
fun WelcomeScreen(onSkip: () -> Unit) {
    Surface(color = AppColors.surface, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Skip button at top-right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text("Skip", color = AppColors.primary)
                }
            }

            // Main content
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            onClick = { /* Navigate to Get Started */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.darkButton)
                        ) {
                            Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { /* Navigate to Log In */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(AppColors.textSecondary)
                            )
                        ) {
                            Text(
                                "I Have an Account",
                                fontSize = 18.sp,
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(onSkip={})
    }
}
