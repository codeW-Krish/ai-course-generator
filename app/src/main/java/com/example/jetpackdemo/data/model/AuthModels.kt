package com.example.jetpackdemo.data.model
import com.google.gson.annotations.SerializedName
// Represents the user object returned by the backend on auth success.
data class User(
    val id: String,
    val email: String,
    val username: String
)

// The complete response from a successful login or register call.
data class AuthResponse(
    val message: String,
    val user: User,
    val accessToken: String,
    val refreshToken: String
)

// The response from a successful token refresh.
// NOTE: Your backend sends 'newAccessToken' and 'newRefreshToken'.
// We use @SerializedName to map these to our standard field names.
// You will need to add the 'com.google.code.gson:gson' dependency for this.

data class RefreshResponse(
    val message: String,
    @SerializedName("newAccessToken")
    val accessToken: String,
    @SerializedName("newRefreshToken")
    val refreshToken: String
)

// Data class for the /auth/register endpoint request body.
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

// Data class for the /auth/login endpoint request body.
data class LoginRequest(
    val email: String,
    val password: String
)

// The request to get a new access token.
data class RefreshRequest(
    val refreshToken: String
)
