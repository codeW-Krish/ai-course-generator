package com.example.jetpackdemo

import android.util.Log
import android.util.Patterns
import org.json.JSONObject

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.jetpackdemo.data.api.RetrofitClient
import com.example.jetpackdemo.data.model.LoginRequest
import com.example.jetpackdemo.shared_pref.UserPreferencesManager
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.utils.TokenManager
import com.example.jetpackdemo.viewmodels.CourseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, courseViewModel: CourseViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }
    val userPrefsManager = remember { UserPreferencesManager(context) }  // ← NEW

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome Back!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
            Text(
                "Log in to continue your learning journey.",
                fontSize = 16.sp,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = AppColors.textSecondary)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // === BASIC VALIDATION ===
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.publicApi.login(LoginRequest(email, password))

                            if (response.isSuccessful) {
                                val body = response.body()!!

                                // === SAVE TOKEN (contract-compatible + legacy fallback) ===
                                val authToken = body.token ?: body.accessToken
                                if (!authToken.isNullOrBlank()) {
                                    tokenManager.saveIdToken(authToken)
                                }

                                // === SAVE USER ROLE (Critical for RBAC) ===
                                body.user.role?.let { role ->
                                    userPrefsManager.saveUserRole(role)
                                } ?: userPrefsManager.saveUserRole("user")

                                val role = userPrefsManager.getUserRole()
                                userPrefsManager.saveUserData(body.user.id, body.user.username, role, email)
                                courseViewModel.reloadUserRole()
                                courseViewModel.reloadUserData()

                                // === NAVIGATE TO MAIN ===
                                navController.navigate("main") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val errorMessage = parseErrorMessage(errorBody)
                                Toast.makeText(context, "Login failed: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("LOGIN_ERROR", "Network failed", e)
                            val msg = when (e) {
                                is java.net.UnknownHostException -> "No internet or wrong URL"
                                is java.net.ConnectException -> "Server not reachable"
                                is javax.net.ssl.SSLHandshakeException -> "SSL error (ngrok cert)"
                                else -> e.message ?: "Unknown error"
                            }
                            Toast.makeText(context, "Login failed: $msg", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AppColors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Log In", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SignUpNavigation {
                navController.navigate("signup")
            }
        }
    }
}

@Composable
private fun SignUpNavigation(onSignUpClicked: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = AppColors.textSecondary, fontSize = 14.sp)) {
            append("Don't have an account? ")
        }
        pushStringAnnotation(tag = "SIGNUP", annotation = "signup")
        withStyle(style = SpanStyle(color = AppColors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
            append("Sign Up")
        }
        pop()
    }
    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "SIGNUP", start = offset, end = offset)
                .firstOrNull()?.let { onSignUpClicked() }
        }
    )
}

fun parseErrorMessage(errorBody: String?): String {
    return try {
        val json = JSONObject(errorBody ?: return "Unknown error")
        json.getString("error") ?: "Unknown error"
    } catch (e: Exception) {
        "Something went wrong"
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun LoginScreenPreview() {
//    LoginScreen(rememberNavController())
//}