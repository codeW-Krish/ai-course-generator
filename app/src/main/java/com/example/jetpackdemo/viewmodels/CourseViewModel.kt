package com.example.jetpackdemo.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.jetpackdemo.data.api.RetrofitClient
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import com.example.jetpackdemo.data.model.SSEEvent
import com.example.jetpackdemo.shared_pref.UserPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.jetpackdemo.data.model.DefaultProvidersResponse


// A sealed class to wrap state (you can reuse this for all API calls)
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

class CourseViewModel(
    private val repository: CourseRepository,
    application: Application  // Use Application, not Activity
) : AndroidViewModel(application) {  // Change to AndroidViewModel

    // Now you can access context safely
    private val context: Context = application
    private val _content = MutableLiveData<Resource<ContentGenerationStatusResponse>>()
    val content: LiveData<Resource<ContentGenerationStatusResponse>> = _content

    private val _status = MutableLiveData<Resource<GenerationStatusResponse>>()
    val status: LiveData<Resource<GenerationStatusResponse>> = _status

    private val _publicCourses = MutableLiveData<Resource<CoursesResponse>>()
    val publicCourses: LiveData<Resource<CoursesResponse>> = _publicCourses

    private val _myCourses = MutableLiveData<Resource<MyCoursesResponse>>()
    val myCourses: LiveData<Resource<MyCoursesResponse>> = _myCourses

    private val _enrolledCourses = MutableLiveData<Resource<enrolledCoursesResponse>>()
    val enrolledCourses: LiveData<Resource<enrolledCoursesResponse>> = _enrolledCourses

    private val _enrollResult = MutableLiveData<Resource<EnrollResponse>>()
    val enrollResult: LiveData<Resource<EnrollResponse>> = _enrollResult

    // For generation status polling
    private val _generationStatus = MutableStateFlow<Resource<GenerationStatusResponse>?>(null)
    val generationStatus = _generationStatus.asStateFlow()

    // For full course content
    private val _fullCourseContent =
        MutableStateFlow<Resource<CourseFullResponse>?>(Resource.Loading())
    val fullCourseContent = _fullCourseContent.asStateFlow()


    private val _outlineState = MutableStateFlow<CourseOutline?>(null)
    val outlineState: StateFlow<CourseOutline?> = _outlineState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _updateOutline = MutableStateFlow<Resource<GenerateOutlineResponse>?>(null)
    val updateOutline = _updateOutline.asStateFlow()

    private val _courseId = MutableStateFlow<String?>(null)
    val courseId = _courseId.asStateFlow()


    // In CourseViewModel.kt
    fun setCourseId(id: String) {
        _courseId.value = id
    }

    // Add these state flows to your CourseViewModel
    private val _isGeneratingContent = MutableStateFlow(false)
    val isGeneratingContent = _isGeneratingContent.asStateFlow()

    private val _generationStartTime = MutableStateFlow<Long?>(null)
    val generationStartTime = _generationStartTime.asStateFlow()


    // Add these states
    private val _streamingEvents = MutableSharedFlow<SSEEvent?>(
        replay = 1, // Keep last event
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val streamingEvents = _streamingEvents.asSharedFlow()

    private val _generationProgress = MutableStateFlow<GenerationProgress?>(null)
    val generationProgress = _generationProgress.asStateFlow()


    private val _currentStreamingText = MutableStateFlow("")
    val currentStreamingText = _currentStreamingText.asStateFlow()

    private val _currentStreamingSubtopic = MutableStateFlow<String?>(null)
    val currentStreamingSubtopic = _currentStreamingSubtopic.asStateFlow()

    // Polling state
    private val _isPollingActive = MutableStateFlow(false)
    val isPollingActive = _isPollingActive.asStateFlow()

    private var pollingJob: Job? = null

    // ADD THESE NEW PROPERTIES RIGHT AFTER pollingJob:
    private val userPrefsManager = UserPreferencesManager(application)

    private val _selectedContentProvider = MutableStateFlow("Groq")
    val selectedContentProvider = _selectedContentProvider.asStateFlow()

    private val _selectedOutlineProvider = MutableStateFlow("Groq")
    val selectedOutlineProvider = _selectedOutlineProvider.asStateFlow()

    private val _isStreamingProvider = MutableStateFlow(true)
    val isStreamingProvider = _isStreamingProvider.asStateFlow()



    // ADD THIS INIT BLOCK (place it after all property declarations):
    // Add these state flows to your CourseViewModel
    private val _availableProviders = MutableStateFlow<List<String>>(emptyList())
    val availableProviders = _availableProviders.asStateFlow()

    private val _defaultProviders = MutableStateFlow<DefaultProvidersResponse?>(null)
    val defaultProviders = _defaultProviders.asStateFlow()

    private val _userRole = MutableStateFlow("user")
    val userRole = _userRole.asStateFlow()


    private val _username = MutableStateFlow("User")  // Default
    val username = _username.asStateFlow()


    // Update the init block
    init {
        viewModelScope.launch {
            // Load user role
            _userRole.value = userPrefsManager.getUserRole()

            // Load available providers
            loadAvailableProviders()

            // Load default providers
            loadDefaultProviders()

            // Validate and fix user's providers against available list
            validateAndFixUserProviders()

            // Load user's personal providers
            val contentProvider = userPrefsManager.getContentProvider()
            val outlineProvider = userPrefsManager.getOutlineProvider()

            _selectedContentProvider.value = contentProvider ?: _defaultProviders.value?.content ?: "Groq"
            _selectedOutlineProvider.value = outlineProvider ?: _defaultProviders.value?.outline ?: "Groq"
            _isStreamingProvider.value = !_selectedContentProvider.value.equals("cerebras", ignoreCase = true)

            Log.d("VIEWMODEL", "Loaded providers - Content: ${_selectedContentProvider.value}, Outline: ${_selectedOutlineProvider.value}")
        }
    }

    // Add these methods to your CourseViewModel
    fun loadAvailableProviders() {
        viewModelScope.launch {
            try {
                val response = repository.getAvailableProviders()
                if (response.isSuccessful) {
                    _availableProviders.value = response.body()?.providers ?: emptyList()
                    validateAndFixUserProviders()  // ADD THIS LINE
            }
            } catch (e: Exception) {
                Log.e("VIEWMODEL", "Failed to load available providers", e)
            }
        }
    }

    fun loadDefaultProviders() {
        viewModelScope.launch {
            try {
                val response = repository.getDefaultProviders()
                if (response.isSuccessful) {
                    _defaultProviders.value = response.body()
                }
            } catch (e: Exception) {
                Log.e("VIEWMODEL", "Failed to load default providers", e)
            }
        }
    }

    private fun validateAndFixUserProviders() {
        viewModelScope.launch {
            val available = _availableProviders.value
            if (available.isEmpty()) return@launch // Wait for load

            val needsFix = userPrefsManager.validateAndFixProviders(available)
            if (needsFix) {
                Log.d("VIEWMODEL", "User providers were invalid → reset to defaults")

                // Force UI to use defaults
                val defaultContent = _defaultProviders.value?.content ?: "Groq"
                val defaultOutline = _defaultProviders.value?.outline ?: "Groq"

                _selectedContentProvider.value = defaultContent
                _selectedOutlineProvider.value = defaultOutline
                _isStreamingProvider.value = !defaultContent.equals("cerebras", ignoreCase = true)

                // Optionally save defaults so next launch is clean
                userPrefsManager.saveContentProvider(defaultContent)
                userPrefsManager.saveOutlineProvider(defaultOutline)
            }
        }
    }

    fun updateUserRole(role: String) {
        userPrefsManager.saveUserRole(role)
        _userRole.value = role
    }

    fun reloadUserRole() {
        viewModelScope.launch {
            _userRole.value = userPrefsManager.getUserRole()
            Log.d("CourseVM", "Reloaded role: ${_userRole.value}")
        }
    }

    fun reloadUserData(){
        viewModelScope.launch {
            _username.value = userPrefsManager.getUsername() ?: "User"
        }
    }

    fun refreshProviders() {
        // This function should be called every time the screen is shown
        loadAvailableProviders()
        loadDefaultProviders()
    }



    private val _searchResults = MutableStateFlow<List<SearchItem>>(emptyList())
    val searchResults: StateFlow<List<SearchItem>> = _searchResults

    private val _fullSearchResults = MutableStateFlow<List<FullSearchItem>>(emptyList())
    val fullSearchResults: StateFlow<List<FullSearchItem>> = _fullSearchResults

    private val _notes = mutableStateMapOf<String, String>()
    val notes: Map<String, String> = _notes

    private val _progress = mutableStateMapOf<String, Boolean>()
    val progress: Map<String, Boolean> = _progress

    // Real-time dropdown search
    fun searchCourses(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            val response = repository.searchCourses(query)
            if (response.isSuccessful) {
                _searchResults.value = response.body()?.courses ?: emptyList()
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    // Full search with filters
    fun searchCoursesFull(query: String, difficulty: String?, sortBy: String?) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _fullSearchResults.value = emptyList()
                return@launch
            }
            val response = repository.searchCoursesFull(query, difficulty, sortBy)
            if (response.isSuccessful) {
                _fullSearchResults.value = response.body()?.courses ?: emptyList()
            } else {
                _fullSearchResults.value = emptyList()
            }
        }
    }

    // Delete own course
    fun deleteMyCourse(courseId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val response = repository.deleteMyCourse(courseId)
            if (response.isSuccessful) {
                onSuccess()
                getMyCourses() // refresh list
            }
        }
    }

    // Save note
    fun saveNote(subtopicId: String, note: String) {
        viewModelScope.launch {
            val response = repository.saveNote(subtopicId, note)
            if (response.isSuccessful) {
                _notes[subtopicId] = note
            }
        }
    }

    // Load note
    fun loadNote(subtopicId: String) {
        viewModelScope.launch {
            val response = repository.getNote(subtopicId)
            if (response.isSuccessful) {
                response.body()?.note?.let { _notes[subtopicId] = it }
            }
        }
    }

    // Toggle complete
    fun toggleComplete(subtopicId: String, completed: Boolean) {
        viewModelScope.launch {
            val response = repository.markComplete(subtopicId, completed)
            if (response.isSuccessful) {
                _progress[subtopicId] = completed
            }
        }
    }

    // Load course progress
    fun loadCourseProgress(courseId: String) {
        viewModelScope.launch {
            val response = repository.getCourseProgress(courseId)
            if (response.isSuccessful) {
                response.body()?.forEach { _progress[it.subtopic_id] = it.completed }
            }
        }
    }



    data class GenerationProgress(
        val generated: Int,
        val total: Int,
        val subtopic: String,
        val unit: String,
        val progress: Int
    )

    // Add this function


    // FIXED: Proper polling logic with correct content detection
    fun startPollingFullContent(courseId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _isPollingActive.value = true
            var pollCount = 0
            val maxPolls = 70 // 60 seconds maximum

            Log.d("POLLING", "Starting polling for course: $courseId")

            while (isActive && pollCount < maxPolls) {
                try {
                    val response = repository.getFullCourse(courseId)
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        // FIXED: Check if content exists and is not empty/null
                        val hasGeneratedContent = data.units.flatMap { it.subtopics }
                            .any { subtopic ->
                                subtopic.content != null &&
                                        subtopic.content != "null" &&
                                        subtopic.content.isNotEmpty() &&
                                        subtopic.content != "[]" &&
                                        subtopic.content != "Content is being generated..."
                            }

                        Log.d("POLLING", "Poll $pollCount - Has content: $hasGeneratedContent")

                        _fullCourseContent.value = Resource.Success(data)

                        if (hasGeneratedContent) {
                            Log.d("POLLING", "✅ Content generated! Stopping polling")
                            stopStreaming()
                            _isPollingActive.value = false
                            break
                        }
                    } else {
                        Log.e("POLLING", "HTTP Error: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("POLLING", "Failed to poll", e)
                }

                delay(2000)
                pollCount++

                // Log progress
                if (pollCount % 5 == 0) {
                    Log.d("POLLING", "Still polling... attempt $pollCount/$maxPolls")
                }
            }

            if (pollCount >= maxPolls) {
                Log.w("POLLING", "❌ Polling timeout - no content generated after 60 seconds")
                stopStreaming()
            }
            _isPollingActive.value = false
            Log.d("POLLING", "Polling stopped")
        }
    }

    fun isPollingActive(): Boolean = _isPollingActive.value


    // 1000% WORKING
    fun startStreamingGeneration(courseId: String, provider: String? = null, model: String? = null) {
        Log.d("STREAMING", "🚀 STARTING STREAMING for courseId: $courseId")

        viewModelScope.launch(Dispatchers.IO) {
            // Reset all streaming states
            withContext(Dispatchers.Main) {
                _currentStreamingText.value = ""
                _currentStreamingSubtopic.value = null
                _isGeneratingContent.value = true
                _generationProgress.value = null
                _generationStartTime.value = System.currentTimeMillis()
                _isPollingActive.value = false // Ensure polling is stopped when starting new stream
            }

            val client = RetrofitClient.getOkHttpClientForSSE(context)

            val requestBody = buildString {
                append("{\n")
                append("  \"provider\": \"${provider ?: "Groq"}\"")
                if (model != null) {
                    append(",\n  \"model\": \"$model\"")
                }
                append("\n}")
            }

            val body = requestBody.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${RetrofitClient.BASE_URL}api/courses/$courseId/generate-content-stream")
                .post(body)
                .build()

            Log.d("STREAMING", "📡 SSE URL: ${request.url}")
            Log.d("STREAMING", "📦 Request Body: $requestBody")

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("STREAMING", "❌ SSE Failed: HTTP ${response.code}")
                        withContext(Dispatchers.Main) {
                            _streamingEvents.tryEmit(SSEEvent(type = "error", message = "HTTP ${response.code}"))
                            stopStreaming()
                        }
                        return@launch
                    }

                    Log.d("STREAMING", "✅ SSE Connected, starting to read stream...")

                    response.body?.byteStream()?.bufferedReader()?.use { reader ->
                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            if (line == null) break

                            Log.d("SSE_RAW", line!!)

                            if (line!!.startsWith("data:")) {
                                val json = line!!.removePrefix("data:").trim()
                                if (json == "[DONE]") continue

                                try {
                                    val event = Gson().fromJson(json, SSEEvent::class.java)
                                    Log.d("SSE_PARSED", "📨 Type: ${event.type}, Chunk: '${event.chunk?.take(50)}...'")

                                    // Log detailed event info
                                    when (event.type) {
                                        "start" -> Log.d("STREAMING", "🎬 Stream started")
                                        "chunk" -> Log.d("STREAMING", "📝 Chunk received for: ${event.subtopic}")
                                        "progress" -> Log.d("STREAMING", "📊 Progress: ${event.progress}%")
                                        "complete" -> Log.d("STREAMING", "✅ Stream completed")
                                    }

                                    when (event.type) {
//                                        "chunk" -> {
//                                            val chunk = event.chunk.orEmpty()
//                                            if (chunk.isNotBlank()) {
//                                                withContext(Dispatchers.Main) {
//                                                    _currentStreamingSubtopic.value = event.subtopic
//                                                    _currentStreamingText.value += chunk
//                                                    // Force UI update
//                                                    _streamingEvents.tryEmit(event)
//                                                }
//                                            }
//                                        }
                                        "chunk" -> {
                                            val chunk = event.chunk.orEmpty()
                                            val subtopic = event.subtopic.orEmpty()

                                            if (chunk.isNotBlank()) {
                                                // UPDATE IMMEDIATELY - use update for StateFlow to ensure UI sees changes
                                                viewModelScope.launch(Dispatchers.Main) {
                                                    _currentStreamingText.update { current ->
                                                        Log.d("STREAMING_UI", "📝 Adding chunk: ${chunk.take(30)}...")
                                                        current + chunk
                                                    }
                                                    if (subtopic.isNotBlank() && subtopic != _currentStreamingSubtopic.value) {
                                                        _currentStreamingSubtopic.value = subtopic
                                                    }
                                                    _streamingEvents.tryEmit(event)
                                                }
                                            }
                                        }
                                        "progress" -> {
                                            withContext(Dispatchers.Main) {
                                                updateProgress(event)
                                                if (event.subtopic != _currentStreamingSubtopic.value) {
                                                    _currentStreamingText.value = ""
                                                    _currentStreamingSubtopic.value = event.subtopic
                                                }
                                                _streamingEvents.tryEmit(event)
                                            }
                                        }
                                        "start" -> {
                                            withContext(Dispatchers.Main) {
                                                _streamingEvents.tryEmit(event)
                                            }
                                        }
                                        "complete" -> {
                                            withContext(Dispatchers.Main) {
                                                Log.d("STREAMING", "🎉 Streaming complete - starting polling")
                                                _isGeneratingContent.value = false
                                                _generationProgress.value = null
                                                _streamingEvents.tryEmit(event)
                                                // Start polling to check for saved content
                                                startPollingFullContent(courseId)
                                            }
                                        }
                                        "error" -> {
                                            withContext(Dispatchers.Main) {
                                                Log.e("STREAMING", "❌ Stream error: ${event.message}")
                                                stopStreaming()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("SSE", "❌ Parse error: $json", e)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("STREAMING", "❌ SSE Failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _streamingEvents.tryEmit(SSEEvent(type = "error", message = e.message))
                    stopStreaming()
                }
            }

        }
    }
//
    fun updateProgress(event: SSEEvent) {
        if (event.type == "progress") {
            _generationProgress.value = GenerationProgress(
                generated = event.generated ?: 0,
                total = event.total ?: 0,
                subtopic = event.subtopic ?: "",
                unit = event.unit ?: "",
                progress = event.progress ?: 0
            )
        }
    }

    fun stopStreaming() {
        viewModelScope.launch {
            Log.d("STREAMING", "🛑 Stopping streaming and cleaning up state")
            _isGeneratingContent.value = false
            _generationProgress.value = null
            _currentStreamingText.value = ""
            _currentStreamingSubtopic.value = null
            _isPollingActive.value = false
            _streamingEvents.tryEmit(null)
            pollingJob?.cancel()
            Log.d("STREAMING", "✅ Streaming fully stopped")
        }
    }

    override fun onCleared() {
        Log.d("VIEWMODEL", "🧹 ViewModel cleared - stopping streaming")
        stopStreaming()
        super.onCleared()
    }


    fun clearCourseState() {
        viewModelScope.launch {
            Log.d("VIEWMODEL", "🧹 Clearing course state")
            _isGeneratingContent.value = false
            _currentStreamingText.value = ""
            _currentStreamingSubtopic.value = null
            _generationProgress.value = null
            _isPollingActive.value = false
            _fullCourseContent.value = Resource.Loading()
            stopStreaming()
        }
    }

    /**
     * Clear outline generation state
     */
    fun clearOutlineState() {
        viewModelScope.launch {
            Log.d("VIEWMODEL", "🧹 Clearing outline state")
            // Clear outline-related states
            _outlineState.value = null
            _updateOutline.value = null
            _courseId.value = null
        }
    }

    /**
     * Generate course content with provider-aware logic
     */
    fun generateCourseContent(courseId: String) {
        viewModelScope.launch {
            val provider = _selectedContentProvider.value
            val isCerebras = provider.equals("cerebras", ignoreCase = true)

            _isStreamingProvider.value = !isCerebras
            _isGeneratingContent.value = true
            _currentStreamingText.value = ""
            _currentStreamingSubtopic.value = null

            if (isCerebras) {
                Log.d("GENERATION", "🚀 Starting non-streaming batch for Cerebras")
            } else {
                Log.d("GENERATION", "📡 Starting streaming batches for $provider")
            }

            // Start the generation process
            startStreamingGeneration(courseId, provider)

            // Start polling to check when content is ready
//            startPollingFullContent(courseId)
        }
    }

    /**
     * Update providers when user changes them in settings
     */
    // Update the updateProviders method to validate against available providers
    fun updateProviders(contentProvider: String, outlineProvider: String) {
        viewModelScope.launch {
            // Validate against available providers
            if (!_availableProviders.value.contains(contentProvider)) {
                Log.e("VIEWMODEL", "Invalid content provider: $contentProvider")
                return@launch
            }

            if (!_availableProviders.value.contains(outlineProvider)) {
                Log.e("VIEWMODEL", "Invalid outline provider: $outlineProvider")
                return@launch
            }

            _selectedContentProvider.value = contentProvider
            _selectedOutlineProvider.value = outlineProvider
            _isStreamingProvider.value = !contentProvider.equals("cerebras", ignoreCase = true)

            // Save to preferences
            userPrefsManager.saveContentProvider(contentProvider)
            userPrefsManager.saveOutlineProvider(outlineProvider)

            Log.d(
                "VIEWMODEL",
                "✅ Updated providers - Content: $contentProvider, Outline: $outlineProvider"
            )
        }
    }

    fun clearUserData() {
        viewModelScope.launch {
            userPrefsManager.clearAll()
            clearCourseState()
            clearOutlineState()
            _selectedContentProvider.value = "Groq"
            _selectedOutlineProvider.value = "Groq"
            _isStreamingProvider.value = true
            _userRole.value = "user"
            Log.d("VIEWMODEL", "All user data cleared")
        }
    }

    fun clearPrefManager(){
        viewModelScope.launch {
            userPrefsManager.clearAll()
        }
    }



    private var request: GenerateOutlineRequest? = null
    fun prepareOutlineRequest(
        title: String,
        description: String,
        numUnits: Int,
        difficulty: String,
        includeVideos: Boolean,
        provider: String,
        model: String?
    ) {
        request = GenerateOutlineRequest(
            title = title,
            description = description,
            numUnits = numUnits,
            difficulty = difficulty,
            includeVideos = includeVideos,
            provider = provider,
            model = model
        )
    }
    // ---- API CALLS ----

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun generateCourseOutline() {
        val req = request ?: return
        viewModelScope.launch {
            _isLoading.value = true
//            _outline.value = Resource.Loading()
            try {
                val response = repository.generateCourseOutline(req)
                Log.d("CourseViewModel", "Response code: ${response.code()}")
                if (response.isSuccessful) {
//                    _outline.value = Resource.Success(response.body()!!)
                    val body = response.body()
                    Log.d("CourseViewModel", "Response body: $body")
                    _courseId.value = response.body()?.courseId
                    _outlineState.value = response.body()?.outline
                } else {
//                    _outlineState.value = Resource.Error("Failed: ${response.code()}")
                    Log.e("CourseViewModel", "Error response: ${response.errorBody()?.string()}")
                    _outlineState.value = null
                }
            } catch (e: Exception) {
//                _outlineState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
                Log.e("CourseViewModel", "Exception in generateCourseOutline", e)
                _outlineState.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCourseOutlineBeforeGeneration(
        courseId: String,
        outline: CourseOutline,
        regenerate: Boolean = false
    ) {
        viewModelScope.launch {
            _updateOutline.value = Resource.Loading();
            try {
                val response =
                    repository.updateOutlineBeforeGenerationConetent(courseId, outline, regenerate);
                if (response.isSuccessful) {
                    _updateOutline.value = Resource.Success(response.body()!!)
                    _outlineState.value = response.body()?.outline
                } else {
                    _updateOutline.value =
                        Resource.Error("Failed to update outline: ${response.code()}")
                }
            } catch (e: Exception) {
                _updateOutline.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }




//


    fun getFullCourseContent(courseId: String) {
        viewModelScope.launch {
            val current = _fullCourseContent.value
            val previousData = if (current is Resource.Success) current.data else null
            _fullCourseContent.value = Resource.Loading(previousData)


            try {
                val response = repository.getFullCourse(courseId)
                if (response.isSuccessful && response.body() != null) {
                    _fullCourseContent.value = Resource.Success(response.body()!!)
                } else {
                    _fullCourseContent.value = Resource.Error("HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                _fullCourseContent.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

//    }



    fun getAllPublicCourses() {
        viewModelScope.launch {
            _publicCourses.value = Resource.Loading()
            try {
                val response = repository.getAllPublicCourses()
                if (response.isSuccessful) {
                    _publicCourses.value = Resource.Success(response.body()!!)
                } else {
                    _publicCourses.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _publicCourses.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

//    fun getAllPublicCourses() {
//        viewModelScope.launch {
//            _publicCourses.postValue(Resource.Loading)
//            try {
//                val response = repository.getAllPublicCourses()
//                if (response.isSuccessful && response.body() != null) {
//                    _publicCourses.postValue(Resource.Success(response.body()!!))
//                } else {
//                    _publicCourses.postValue(Resource.Error("Failed: ${response.code()}"))
//                }
//            } catch (e: Exception) {
//                _publicCourses.postValue(Resource.Error(e.message ?: "Unknown error"))
//            }
//        }
//    }

    fun getMyCourses() {
        viewModelScope.launch {
            _myCourses.value = Resource.Loading()
            try {
                val response = repository.getMyCourses()
                if (response.isSuccessful) {
                    _myCourses.value = Resource.Success(response.body()!!)
                } else {
                    _myCourses.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _myCourses.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun getEnrolledCourses() {
        viewModelScope.launch {
            _enrolledCourses.postValue(Resource.Loading())  // ← postValue
            try {
                val response = repository.getEnrolledCourses()  // ← Response<enrolledCoursesResponse>
                if (response.isSuccessful && response.body() != null) {
                    _enrolledCourses.postValue(Resource.Success(response.body()!!))
                } else {
                    _enrolledCourses.postValue(Resource.Error("Failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                _enrolledCourses.postValue(Resource.Error(e.localizedMessage ?: "Unknown error"))
            }
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _enrollResult.value = Resource.Loading()
            try {
                val response = repository.enrollInCourse(courseId)
                if (response.isSuccessful) {
                    _enrollResult.value = Resource.Success(response.body()!!)
                    getEnrolledCourses()
                } else {
                    _enrollResult.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _enrollResult.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }


    fun clearEnrollResult() {
        _enrollResult.value = Resource.Loading()
    }

}
