package com.example.jetpackdemo.data.model
import com.google.gson.annotations.SerializedName
// Represents the user object returned by the backend on auth success.
data class User(
    val id: String,
    val email: String,
    val username: String,
    val role: String = "user" // Add role with default
)

data class Users(
    val id: String,
    val username: String
)

// AuthResponse remains the same but User now includes role
data class AuthResponse(
    val message: String,
    val user: User,
    @SerializedName("token")
    val token: String? = null,
    @SerializedName("accessToken")
    val accessToken: String? = null,
    @SerializedName("refreshToken")
    val refreshToken: String? = null
)

data class RefreshValidationResponse(
    val message: String,
    val user: User
)

// Add new data classes for admin features
data class GlobalSettingsResponse(
    val settings: List<GlobalSetting>
)

data class GlobalSetting(
    val key: String,
    val value: String,
    val description: String?,
    val updated_at: String?
)

data class AvailableProvidersResponse(
    val providers: List<String>
)

data class DefaultProvidersResponse(
    val outline: String,
    val content: String
)

data class UpdateProvidersRequest(
    val providers: List<String>
)

data class UpdateDefaultProvidersRequest(
    val outlineProvider: String,
    val contentProvider: String
)

// Data class for the /auth/register endpoint request body.
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

// Data class for the /auth/login endpoint request body.
data class LoginRequest(
    val email: String,
    val password: String
)