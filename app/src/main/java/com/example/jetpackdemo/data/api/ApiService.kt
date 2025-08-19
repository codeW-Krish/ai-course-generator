package com.example.jetpackdemo.data.api

import com.example.jetpackdemo.data.model.AuthResponse
import com.example.jetpackdemo.data.model.LoginRequest
import com.example.jetpackdemo.data.model.RefreshRequest
import com.example.jetpackdemo.data.model.RefreshResponse
import com.example.jetpackdemo.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<RefreshResponse>
}

