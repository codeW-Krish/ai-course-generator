package com.example.jetpackdemo.data.api

import com.example.jetpackdemo.data.model.AuthResponse
import com.example.jetpackdemo.data.model.ContentGenerationStatusResponse
import com.example.jetpackdemo.data.model.CourseFullResponse
import com.example.jetpackdemo.data.model.CourseOutline
import com.example.jetpackdemo.data.model.CoursesResponse
import com.example.jetpackdemo.data.model.EnrollResponse
import com.example.jetpackdemo.data.model.GenerateOutlineRequest
import com.example.jetpackdemo.data.model.GenerateOutlineResponse
import com.example.jetpackdemo.data.model.GeneratedSubtopicContent
import com.example.jetpackdemo.data.model.GenerationStatusResponse
import com.example.jetpackdemo.data.model.LoginRequest
import com.example.jetpackdemo.data.model.RefreshRequest
import com.example.jetpackdemo.data.model.RefreshResponse
import com.example.jetpackdemo.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    // Update course outline by ID
    @POST("/api/courses/{id}/outline/regenerate")
    suspend fun updateCourseOutline(
        @Path("id") courseId: String,
        @Body outline: CourseOutline,
        @Query("regenerate") regenerate: Boolean = false
    ): Response<GenerateOutlineResponse> // or maybe just a status response — check backend return type


    // Generate Content (triggers generation job)
    @POST("/api/courses/{id}/generate-content")
    suspend fun generateContent(
        @Path("id") courseId: String,
        @Query("provider") provider: String? = null,
        @Query("model") model: String? = null
    ): Response<ContentGenerationStatusResponse>


    // Get Generation Response
    @GET("/api/courses/{id}/generation-status")
    suspend fun getCourseGenerationStatus(@Path("id") courseId: String, @retrofit2.http.Query("since") since: String? = null): Response<GenerationStatusResponse>

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

