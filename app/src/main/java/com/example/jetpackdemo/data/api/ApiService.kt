package com.example.jetpackdemo.data.api

import com.example.jetpackdemo.data.model.AuthResponse
import com.example.jetpackdemo.data.model.AvailableProvidersResponse
import com.example.jetpackdemo.data.model.ContentGenerationStatusResponse
import com.example.jetpackdemo.data.model.CourseFullResponse
import com.example.jetpackdemo.data.model.CourseOutline
import com.example.jetpackdemo.data.model.CoursesResponse
import com.example.jetpackdemo.data.model.DefaultProvidersResponse
import com.example.jetpackdemo.data.model.EnrollResponse
import com.example.jetpackdemo.data.model.FullSearchResponse
import com.example.jetpackdemo.data.model.GenerateOutlineRequest
import com.example.jetpackdemo.data.model.GenerateOutlineResponse
import com.example.jetpackdemo.data.model.GeneratedSubtopicContent
import com.example.jetpackdemo.data.model.GenerationStatusResponse
import com.example.jetpackdemo.data.model.GlobalSettingsResponse
import com.example.jetpackdemo.data.model.InteractiveChatRequest
import com.example.jetpackdemo.data.model.InteractiveChatResponse
import com.example.jetpackdemo.data.model.InteractiveSessionResponse
import com.example.jetpackdemo.data.model.LoginRequest
import com.example.jetpackdemo.data.model.MyCoursesResponse
import com.example.jetpackdemo.data.model.NoteResponse
import com.example.jetpackdemo.data.model.ProgressItem
import com.example.jetpackdemo.data.model.RefreshValidationResponse
import com.example.jetpackdemo.data.model.RegisterRequest
import com.example.jetpackdemo.data.model.SearchResponse
import com.example.jetpackdemo.data.model.UpdateDefaultProvidersRequest
import com.example.jetpackdemo.data.model.UpdateProvidersRequest
import com.example.jetpackdemo.data.model.VerifyAnswerRequest
import com.example.jetpackdemo.data.model.VerifyAnswerResponse
import com.example.jetpackdemo.data.model.enrolledCoursesResponse
import com.example.jetpackdemo.data.model.GetFlashcardsResponse
import com.example.jetpackdemo.data.model.ReviewFlashcardRequest
import com.example.jetpackdemo.data.model.ReviewFlashcardResponse
import com.example.jetpackdemo.data.model.GetDueFlashcardsResponse
import com.example.jetpackdemo.data.model.GetGeneratedNotesResponse
import com.example.jetpackdemo.data.model.ExportSubtopicNotesResponse
import com.example.jetpackdemo.data.model.ExportCourseNotesResponse
import com.example.jetpackdemo.data.model.GetAudioResponse
import com.example.jetpackdemo.data.model.CourseChatRequest
import com.example.jetpackdemo.data.model.CourseChatResponse
import com.example.jetpackdemo.data.model.CoursePracticeRequest
import com.example.jetpackdemo.data.model.CoursePracticeResponse
import com.example.jetpackdemo.data.model.ContentResponse
import com.example.jetpackdemo.data.model.QuizResponse
import com.example.jetpackdemo.data.model.SubmitQuizRequest
import com.example.jetpackdemo.data.model.SubmitQuizResponse
import com.example.jetpackdemo.data.model.HubActivityRequest
import com.example.jetpackdemo.data.model.HubActivity
import com.example.jetpackdemo.data.model.HubHistoryResponse
import com.example.jetpackdemo.data.model.UserProfile
import com.example.jetpackdemo.data.model.UpdateProfileRequest
import com.example.jetpackdemo.data.model.FollowResponse
import com.example.jetpackdemo.data.model.FollowersResponse
import com.example.jetpackdemo.data.model.FollowingResponse
import com.example.jetpackdemo.data.model.GenerateManifestResponse
import com.example.jetpackdemo.data.model.ManifestStatusResponse
import com.example.jetpackdemo.data.model.CourseManifestsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Register
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Login
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Refresh Token
//    @POST("/api/auth/refresh")
//    suspend fun refresh(@Body request: RefreshRequest): Response<RefreshResponse>

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
    suspend fun getMyCourses(): Response<MyCoursesResponse>

    // GET courses enrolled by current user
    @GET("/api/courses/me/enrolled")
    suspend fun getEnrolledCourses(): Response<enrolledCoursesResponse>

    // Enroll in course by ID
    @POST("/api/courses/{id}/enroll")
    suspend fun enrollInCourse(@Path("id") courseId: String): Response<EnrollResponse>

    // Get full course content by courseId (with units & subtopics)
    @GET("/api/courses/{id}/full")
    suspend fun getFullCourse(@Path("id") courseId: String): Response<CourseFullResponse>

    // === ADMIN ENDPOINTS ===
    @GET("/api/admin/courses")
    suspend fun getAllCoursesAdmin(): Response<CoursesResponse>

    @DELETE("/api/admin/courses/{id}")
    suspend fun deleteCourseAdmin(@Path("id") courseId: String): Response<GenericResponse>

    @GET("/api/admin/settings")
    suspend fun getGlobalSettings(): Response<GlobalSettingsResponse>

    @PUT("/api/admin/settings/{key}")
    suspend fun updateGlobalSetting(
        @Path("key") key: String,
        @Body request: Map<String, String>
    ): Response<GenericResponse>

    @PUT("/api/admin/providers/available")
    suspend fun updateAvailableProviders(@Body request: UpdateProvidersRequest): Response<GenericResponse>

    @PUT("/api/admin/providers/default")
    suspend fun updateDefaultProviders(@Body request: UpdateDefaultProvidersRequest): Response<GenericResponse>

    // === PUBLIC SETTINGS ENDPOINTS ===
    @GET("/api/settings/providers/available")
    suspend fun getAvailableProviders(): Response<AvailableProvidersResponse>

    @GET("/api/settings/providers/default")
    suspend fun getDefaultProviders(): Response<DefaultProvidersResponse>


    @GET("/api/courses/search")
    suspend fun searchCourses(@Query("query") query: String): Response<SearchResponse>

    @GET("/api/courses/search/full")
    suspend fun searchFull(
        @Query("query") query: String,
        @Query("difficulty") difficulty: String? = null,
        @Query("sortBy") sortBy: String? = null
    ): Response<FullSearchResponse>

    @DELETE("/api/courses/{id}")
    suspend fun deleteCourse(@Path("id") id: String): Response<GenericResponse>

    @POST("/api/courses/subtopics/{id}/notes")
    suspend fun saveNote(@Path("id") id: String, @Body body: Map<String, String>): Response<GenericResponse>

    @GET("/api/courses/subtopics/{id}/notes")
    suspend fun getNote(@Path("id") id: String): Response<NoteResponse>

    @POST("/api/courses/subtopics/{id}/complete")
    suspend fun markComplete(@Path("id") id: String, @Body body: Map<String, Boolean>): Response<GenericResponse>

    @GET("/api/courses/{id}/progress")
    suspend fun getProgress(@Path("id") id: String): Response<List<ProgressItem>>

    @POST("/api/auth/refresh")
    suspend fun refresh(@Header("Authorization") authorization: String): Response<RefreshValidationResponse>

    // === INTERACTIVE LEARNING MODE ===
    
    /**
     * Get the next uncompleted subtopic for a course.
     * Generates content + questions if not already generated.
     * Returns course_completed: true if all subtopics are done.
     */
    @GET("/api/interactive/course/{courseId}/next")
    suspend fun getNextInteractiveSubtopic(
        @Path("courseId") courseId: String,
        @Query("provider") provider: String = "Groq"
    ): Response<InteractiveSessionResponse>

    /**
     * Get a specific subtopic session (for resuming)
     */
    @GET("/api/interactive/{subtopicId}")
    suspend fun getInteractiveSession(
        @Path("subtopicId") subtopicId: String,
        @Query("provider") provider: String = "Groq"
    ): Response<InteractiveSessionResponse>

    /**
     * Verify answer for a question.
     * If correct and last question, marks subtopic as completed.
     */
    @POST("/api/interactive/{subtopicId}/verify")
    suspend fun verifyAnswer(
        @Path("subtopicId") subtopicId: String,
        @Body request: VerifyAnswerRequest
    ): Response<VerifyAnswerResponse>

    /**
     * Ask AI a question within the context of a subtopic.
     */
    @POST("/api/interactive/{subtopicId}/chat")
    suspend fun sendInteractiveChat(
        @Path("subtopicId") subtopicId: String,
        @Body request: InteractiveChatRequest
    ): Response<InteractiveChatResponse>

    // === CONTENT-FIRST INTERACTIVE FLOW ===

    /** Get content only for next uncompleted subtopic (triggers background quiz gen) */
    @GET("/api/interactive/course/{courseId}/next-content")
    suspend fun getNextContent(
        @Path("courseId") courseId: String,
        @Query("provider") provider: String = "Groq"
    ): Response<ContentResponse>

    /** Get quiz questions with correct answers for client-side checking */
    @GET("/api/interactive/{subtopicId}/quiz")
    suspend fun getQuiz(
        @Path("subtopicId") subtopicId: String,
        @Query("provider") provider: String = "Groq"
    ): Response<QuizResponse>

    /** Submit all quiz results at once */
    @POST("/api/interactive/{subtopicId}/submit-quiz")
    suspend fun submitQuiz(
        @Path("subtopicId") subtopicId: String,
        @Body request: SubmitQuizRequest
    ): Response<SubmitQuizResponse>

    // === HUB ACTIVITY ===

    /** Log a hub feature access */
    @POST("/api/interactive/hub/log")
    suspend fun logHubActivity(
        @Body request: HubActivityRequest
    ): Response<HubActivity>

    /** Get hub activity history for a course */
    @GET("/api/interactive/hub/history/{courseId}")
    suspend fun getHubHistory(
        @Path("courseId") courseId: String,
        @Query("limit") limit: Int = 50
    ): Response<HubHistoryResponse>

    // === COURSE-LEVEL INTERACTIVE ===
    @POST("/api/interactive/course/{courseId}/chat")
    suspend fun chatWithCourseAI(
        @Path("courseId") courseId: String,
        @Body request: CourseChatRequest
    ): Response<CourseChatResponse>

    @POST("/api/interactive/course/{courseId}/practice")
    suspend fun generateCoursePractice(
        @Path("courseId") courseId: String,
        @Body request: CoursePracticeRequest
    ): Response<CoursePracticeResponse>

    // === AUTH: LOGOUT ===
    @POST("/api/auth/logout")
    suspend fun logout(): Response<GenericResponse>

    // === COURSES: UNENROLL ===
    @DELETE("/api/courses/{id}/unenroll")
    suspend fun unenrollFromCourse(@Path("id") courseId: String): Response<GenericResponse>

    // === FLASHCARDS ===
    @GET("/api/flashcards/{subtopicId}")
    suspend fun getFlashcards(
        @Path("subtopicId") subtopicId: String,
        @Query("provider") provider: String? = null,
        @Query("model") model: String? = null
    ): Response<GetFlashcardsResponse>

    @POST("/api/flashcards/{flashcardId}/review")
    suspend fun reviewFlashcard(
        @Path("flashcardId") flashcardId: String,
        @Body request: ReviewFlashcardRequest
    ): Response<ReviewFlashcardResponse>

    @GET("/api/flashcards/course/{courseId}/due")
    suspend fun getDueFlashcards(
        @Path("courseId") courseId: String
    ): Response<GetDueFlashcardsResponse>

    // === AI NOTES ===
    @GET("/api/notes/{subtopicId}/generated")
    suspend fun getGeneratedNotes(
        @Path("subtopicId") subtopicId: String,
        @Query("provider") provider: String? = null,
        @Query("model") model: String? = null
    ): Response<GetGeneratedNotesResponse>

    @GET("/api/notes/{subtopicId}/export")
    suspend fun exportSubtopicNotes(
        @Path("subtopicId") subtopicId: String,
        @Query("format") format: String = "json"
    ): Response<ExportSubtopicNotesResponse>

    @GET("/api/notes/course/{courseId}/export")
    suspend fun exportCourseNotes(
        @Path("courseId") courseId: String,
        @Query("format") format: String = "json"
    ): Response<ExportCourseNotesResponse>

    // === AUDIO ===
    @GET("/api/audio/{subtopicId}")
    suspend fun getSubtopicAudio(
        @Path("subtopicId") subtopicId: String,
        @Query("tts_provider") ttsProvider: String? = null,
        @Query("voice") voice: String? = null,
        @Query("llm_provider") llmProvider: String? = null,
        @Query("llm_model") llmModel: String? = null
    ): Response<GetAudioResponse>

    @GET("/api/audio/course/{courseId}")
    suspend fun getCourseAudio(
        @Path("courseId") courseId: String,
        @Query("tts_provider") ttsProvider: String? = null,
        @Query("voice") voice: String? = null,
        @Query("llm_provider") llmProvider: String? = null,
        @Query("llm_model") llmModel: String? = null
    ): Response<GetAudioResponse>

    // === USER PROFILE ===
    @GET("/api/users/me")
    suspend fun getMyProfile(): Response<UserProfile>

    @PUT("/api/users/me")
    suspend fun updateMyProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserProfile>

    @GET("/api/users/{userId}/profile")
    suspend fun getUserProfile(
        @Path("userId") userId: String
    ): Response<UserProfile>

    @POST("/api/users/{userId}/follow")
    suspend fun followUser(
        @Path("userId") userId: String
    ): Response<FollowResponse>

    @DELETE("/api/users/{userId}/follow")
    suspend fun unfollowUser(
        @Path("userId") userId: String
    ): Response<FollowResponse>

    @GET("/api/users/{userId}/followers")
    suspend fun getFollowers(
        @Path("userId") userId: String
    ): Response<FollowersResponse>

    @GET("/api/users/{userId}/following")
    suspend fun getFollowing(
        @Path("userId") userId: String
    ): Response<FollowingResponse>

    // === VIDEO MANIFEST (Client-Side Playback) ===

    /** Generate presentation manifest for a subtopic (LLM + TTS + assets → CDN) */
    @POST("/api/videos/{subtopicId}/generate")
    suspend fun generateVideoManifest(
        @Path("subtopicId") subtopicId: String,
        @Query("llm_provider") llmProvider: String? = null,
        @Query("tts_provider") ttsProvider: String? = null,
        @Query("voice") voice: String? = null,
        @Query("image_provider") imageProvider: String? = null
    ): Response<GenerateManifestResponse>

    /** Get cached manifest or generation status */
    @GET("/api/videos/{subtopicId}")
    suspend fun getVideoManifest(
        @Path("subtopicId") subtopicId: String
    ): Response<ManifestStatusResponse>

    /** Regenerate manifest (delete cache + regenerate) */
    @POST("/api/videos/{subtopicId}/regenerate")
    suspend fun regenerateVideoManifest(
        @Path("subtopicId") subtopicId: String,
        @Query("llm_provider") llmProvider: String? = null,
        @Query("tts_provider") ttsProvider: String? = null,
        @Query("voice") voice: String? = null,
        @Query("image_provider") imageProvider: String? = null
    ): Response<GenerateManifestResponse>

    /** Get all manifest statuses for a course */
    @GET("/api/videos/course/{courseId}")
    suspend fun getCourseVideoManifests(
        @Path("courseId") courseId: String
    ): Response<CourseManifestsResponse>

}

data class GenericResponse(
    val message: String
)
