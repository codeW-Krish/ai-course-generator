package com.example.jetpackdemo.data.api

import com.example.jetpackdemo.data.model.AuthResponse
import com.example.jetpackdemo.data.model.CourseFullResponse
import com.example.jetpackdemo.data.model.CoursesResponse
import com.example.jetpackdemo.data.model.EnrollResponse
import com.example.jetpackdemo.data.model.GenerateOutlineRequest
import com.example.jetpackdemo.data.model.GenerateOutlineResponse
import com.example.jetpackdemo.data.model.LoginRequest
import com.example.jetpackdemo.data.model.RefreshRequest
import com.example.jetpackdemo.data.model.RefreshResponse
import com.example.jetpackdemo.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
//    @POST("/api/auth/register")
//    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
//
//    @POST("/api/auth/login")
//    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
//
//    @POST("/api/auth/refresh")
//    suspend fun refresh(@Body request: RefreshRequest): Response<RefreshResponse>
//
//    @POST("/api/courses/generate-outline")
//    suspend fun generateOutline(@Body request: GenerateOutlineRequest): Response<GenerateOutlineResponse>
//
//    @GET("/api/courses")
//    suspend fun getAllPublicCourses(): Response<CoursesResponse>
//
//    @GET("/api/courses/me")
//    suspend fun getCoursesCreatedByMe(): Response<CoursesResponse>
//
//    @GET("/api/courses/me/enrolled")
//    suspend fun getEnrolledCourses(): Response<CoursesResponse>
//
//    @POST("/api/courses/{id}/enroll")
//    suspend fun enrollInCourse(@Path("id") courseId: String): Response<EnrollResponse>
//
//    // 📘 Get full course content by courseId (with units & subtopics)
//    @GET("/api/courses/{id}/full")
//    suspend fun getFullCourse(@Path("id") courseId: String): Response<CourseFullResponse>

    // Register
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Login
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Refresh Token
    @POST("/api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<RefreshResponse>

    // Generate Outline
    @POST("/api/courses/generate-outline")
    suspend fun generateOutline(@Body request: GenerateOutlineRequest): Response<GenerateOutlineResponse>

    // GET all public courses
    @GET("/api/courses")
    suspend fun getAllPublicCourses(): Response<CoursesResponse>

    // GET courses created by current user
    @GET("/api/courses/me")
    suspend fun getMyCourses(): Response<CoursesResponse>

    // GET courses enrolled by current user
    @GET("/api/courses/me/enrolled")
    suspend fun getEnrolledCourses(): Response<CoursesResponse>

    // Enroll in course by ID
    @POST("/api/courses/{id}/enroll")
    suspend fun enrollInCourse(@Path("id") courseId: String): Response<EnrollResponse>

    // Get full course content by courseId (with units & subtopics)
    @GET("/api/courses/{id}/full")
    suspend fun getFullCourse(@Path("id") courseId: String): Response<CourseFullResponse>

}

