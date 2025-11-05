package com.example.jetpackdemo.ui.viewmodel

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
import com.example.jetpackdemo.data.model.SSEEvent
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
import java.io.IOException

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

    private val _myCourses = MutableLiveData<Resource<CoursesResponse>>()
    val myCourses: LiveData<Resource<CoursesResponse>> = _myCourses

    private val _enrolledCourses = MutableLiveData<Resource<CoursesResponse>>()
    val enrolledCourses: LiveData<Resource<CoursesResponse>> = _enrolledCourses

    private val _enroll = MutableLiveData<Resource<EnrollResponse>>()
    val enroll: LiveData<Resource<EnrollResponse>> = _enroll

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

    data class GenerationProgress(
        val generated: Int,
        val total: Int,
        val subtopic: String,
        val unit: String,
        val progress: Int
    )

    // Add this function
//    fun startStreamingGeneration(courseId: String, provider: String? = null, model: String? = null) {
//        Log.d("STREAMING", "STARTING for courseId: $courseId, provider: $provider, model: $model")
//        viewModelScope.launch {
//            _currentStreamingText.value = ""
//            _currentStreamingSubtopic.value = null
//            _isGeneratingContent.value = true
//            _generationStartTime.value = System.currentTimeMillis()
//
//            val client = RetrofitClient.getOkHttpClientForSSE(context)
//
//            // BODY JSON
//            val requestBody = """
//            {
//                "provider": "${provider ?: "Groq"}",
//                ${if (model != null) "\"model\": \"$model\"" else ""}
//            }
//        """.trimIndent()
//
//            val body = requestBody.toRequestBody("application/json".toMediaType())
//
//            val request = Request.Builder()
//                .url("${RetrofitClient.BASE_URL}api/courses/$courseId/generate-content-stream")
//                .post(body)
//                .build()
//
//            Log.d("STREAMING", "SSE URL: ${request.url}")
//            Log.d("STREAMING", "Request Body: $requestBody")
//
//            client.newCall(request).enqueue(object : Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    Log.d("STREAMING", "SSE Response: ${response.code}")
//                    if (!response.isSuccessful) {
//                        _streamingEvents.tryEmit(SSEEvent(type = "error", message = "HTTP ${response.code}"))
//                        return
//                    }
//
//                    response.body?.byteStream()?.bufferedReader()?.use { reader ->
//                        var line: String?
////                        while (reader.readLine().also { line = it } != null) {
////                            Log.d("SSE_RAW", line ?: "null")
////                            if (line!!.startsWith("data:")) {
////                                val json = line!!.removePrefix("data:").trim()
////                                try {
////                                    val event = Gson().fromJson(json, SSEEvent::class.java)
////                                    Log.d("SSE_PARSED", event.toString())
////                                    _streamingEvents.tryEmit(event)
////                                } catch (e: Exception) {
////                                    Log.e("SSE", "Parse error: $json", e)
////                                    _streamingEvents.tryEmit(SSEEvent(type = "error", message = "Parse error"))
////                                }
////                            }
////                        }
//                        // Inside the reader loop
//                        while (reader.readLine().also { line = it } != null) {
//                            Log.d("SSE_RAW", line ?: "null")
//                            if (line!!.startsWith("data:")) {
//                                val json = line!!.removePrefix("data:").trim()
//                                if (json == "[DONE]") continue
//
//                                try {
//                                    val event = Gson().fromJson(json, SSEEvent::class.java)
//                                    Log.d("SSE_PARSED", event.toString())
//
//                                    when (event.type) {
//                                        "chunk" -> {
//                                            _currentStreamingSubtopic.value = event.subtopic
//                                            _currentStreamingText.value += event.chunk
//                                        }
//                                        "progress" -> {
//                                            updateProgress(event)
//                                            // Reset text buffer for next subtopic
//                                            if (event.subtopic != _currentStreamingSubtopic.value) {
//                                                _currentStreamingText.value = ""
//                                                _currentStreamingSubtopic.value = event.subtopic
//                                            }
//                                        }
//                                        "complete" -> {
//                                            _streamingEvents.tryEmit(event)
//                                            stopStreaming()
//                                            viewModelScope.launch(Dispatchers.Main) {
//                                                getFullCourseContent(courseId!!)
//                                            }
//                                        }
//                                        else -> _streamingEvents.tryEmit(event)
//                                    }
//                                } catch (e: Exception) {
//                                    Log.e("SSE", "Parse error: $json", e)
//                                }
//                            }
//                        }
//                    }
//                    Log.d("STREAMING", "Stream ended")
//                   _streamingEvents.tryEmit(SSEEvent(type = "complete"))
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.e("STREAMING", "SSE Failed: ${e.message}", e)
//                    _streamingEvents.tryEmit(SSEEvent(type = "error", message = e.message))
//                }
//            })
//        }
//    }

//    private var pollingJob: Job? = null

    //    fun startPollingFullContent(courseId: String) {
//        pollingJob?.cancel()
//        pollingJob = viewModelScope.launch {
//            while (isActive) {
//                try {
//                    val response = repository.getFullCourse(courseId)
//                    if (response.isSuccessful && response.body() != null) {
//                        val data = response.body()!!
//                        val allGenerated = data.units.flatMap { it.subtopics }
//                            .all { it.content != null && it.contentGeneratedAt != null }
//
//                        _fullCourseContent.value = Resource.Success(data)
//
//                        if (allGenerated) {
//                            stopStreaming()
//                            break
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e("POLLING", "Failed to poll", e)
//                }
//                delay(2000)
//            }
//        }
//    }
// Add this to track polling state

    private var pollingJob: Job? = null

    // FIXED: Proper polling logic with correct content detection
    fun startPollingFullContent(courseId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _isPollingActive.value = true
            var pollCount = 0
            val maxPolls = 30 // 60 seconds maximum

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


    //    fun startStreamingGeneration(courseId: String, provider: String? = null, model: String? = null) {
//        Log.d("STREAMING", "STARTING for courseId: $courseId, provider: $provider, model: $model")
//        viewModelScope.launch {
//            // Reset state
//            _currentStreamingText.value = ""
//            _currentStreamingSubtopic.value = null
//            _isGeneratingContent.value = true
//            _generationStartTime.value = System.currentTimeMillis()
//
//            val client = RetrofitClient.getOkHttpClientForSSE(context)
//
//            // FIXED: No trailing comma
//            val requestBody = buildString {
//                append("{\n")
//                append("  \"provider\": \"${provider ?: "Groq"}\"")
//                if (model != null) {
//                    append(",\n  \"model\": \"$model\"")
//                }
//                append("\n}")
//            }
//
//            val body = requestBody.toRequestBody("application/json".toMediaType())
//
//            val request = Request.Builder()
//                .url("${RetrofitClient.BASE_URL}api/courses/$courseId/generate-content-stream")
//                .post(body)
//                .build()
//
//            Log.d("STREAMING", "SSE URL: ${request.url}")
//            Log.d("STREAMING", "Request Body: $requestBody")
//
//            client.newCall(request).enqueue(object : Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    Log.d("STREAMING", "SSE Response: ${response.code}")
//                    if (!response.isSuccessful) {
//                        viewModelScope.launch {
//                            _streamingEvents.tryEmit(SSEEvent(type = "error", message = "HTTP ${response.code}"))
//                            stopStreaming()
//                        }
//                        return
//                    }
//
//                    response.body?.byteStream()?.bufferedReader()?.use { reader ->
//                        var line: String?
//                        while (reader.readLine().also { line = it } != null) {
//                            Log.d("SSE_RAW", line ?: "null")
//                            if (line!!.startsWith("data:")) {
//                                val json = line!!.removePrefix("data:").trim()
//                                if (json == "[DONE]") continue
//
//                                try {
//                                    val event = Gson().fromJson(json, SSEEvent::class.java)
//                                    Log.d("SSE_PARSED", event.toString())
//
//                                    when (event.type) {
//                                        "chunk" -> {
//                                            //_currentStreamingSubtopic.value = event.subtopic
//                                            //_currentStreamingText.value += event.chunk.orEmpty()
//                                            val chunk = event.chunk.orEmpty()
//                                            _currentStreamingSubtopic.value = event.subtopic
//
//                                            // Only update if we have non-empty content
//                                            if (chunk.isNotBlank()) {
//                                                _currentStreamingText.value += chunk
//                                                // This will automatically trigger UI updates
//                                            }
//                                        }
//                                        "progress" -> {
//                                            updateProgress(event)
//                                            if (event.subtopic != _currentStreamingSubtopic.value) {
//                                                _currentStreamingText.value = ""
//                                                _currentStreamingSubtopic.value = event.subtopic
//                                            }
//                                        }
//                                        "complete" -> {
//                                            _streamingEvents.tryEmit(event)
//                                            stopStreaming()
//                                            // FIXED: Safe courseId + launch
//                                            startPollingFullContent(courseId)
//                                        }
//                                        else -> _streamingEvents.tryEmit(event)
//                                    }
//                                } catch (e: Exception) {
//                                    Log.e("SSE", "Parse error: $json", e)
//                                    viewModelScope.launch {
//                                        _streamingEvents.tryEmit(SSEEvent(type = "error", message = "Parse error"))
//                                        stopStreaming()
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    Log.d("STREAMING", "Stream ended")
//                    viewModelScope.launch {
//                        _streamingEvents.tryEmit(SSEEvent(type = "complete"))
//                        stopStreaming()
//                    }
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.e("STREAMING", "SSE Failed: ${e.message}", e)
//                    viewModelScope.launch {
//                        _streamingEvents.tryEmit(SSEEvent(type = "error", message = e.message))
//                        stopStreaming()
//                    }
//                }
//            })
//        }
//    }
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
//            try {
//                client.newCall(request).enqueue(object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        Log.e("STREAMING", "❌ SSE Failed: ${e.message}", e)
//                        viewModelScope.launch(Dispatchers.Main) {
//                            _streamingEvents.tryEmit(SSEEvent(type = "error", message = e.message))
//                            stopStreaming()
//                        }
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        if (!response.isSuccessful) {
//                            Log.e("STREAMING", "❌ SSE Failed: HTTP ${response.code}")
//                            viewModelScope.launch(Dispatchers.Main) {
//                                _streamingEvents.tryEmit(SSEEvent(type = "error", message = "HTTP ${response.code}"))
//                                stopStreaming()
//                            }
//                            return
//                        }
//
//                        Log.d("STREAMING", "✅ SSE Connected, streaming data...")
//                        val source = response.body?.source() ?: return
//
//                        try {
//                            val buffer = okio.Buffer()
//
//                            while (!source.exhausted()) {
//                                val bytesRead = source.read(buffer, 8192)
//                                if (bytesRead == -1L) break
//
//                                val chunk = buffer.readUtf8()
//                                val lines = chunk.split("\n")
//
//                                for (line in lines) {
//                                    if (line.startsWith("data:")) {
//                                        val json = line.removePrefix("data:").trim()
//                                        if (json == "[DONE]") continue
//
//                                        try {
//                                            val event = Gson().fromJson(json, SSEEvent::class.java)
//                                            Log.d("SSE_PARSED", "📨 Type: ${event.type}, Chunk: '${event.chunk?.take(50)}...'")
//
//                                            // Keep your existing event handling logic here
//                                            when (event.type) {
//                                                "chunk" -> {
//                                                    val chunkText = event.chunk.orEmpty()
//                                                    val subtopic = event.subtopic.orEmpty()
//
//                                                    if (chunkText.isNotBlank()) {
//                                                        viewModelScope.launch(Dispatchers.Main) {
//                                                            _currentStreamingText.update { current ->
//                                                                Log.d("STREAMING_UI", "📝 Adding chunk: ${chunkText.take(30)}...")
//                                                                current + chunkText
//                                                            }
//                                                            if (subtopic.isNotBlank() && subtopic != _currentStreamingSubtopic.value) {
//                                                                _currentStreamingSubtopic.value = subtopic
//                                                            }
//                                                            _streamingEvents.tryEmit(event)
//                                                        }
//                                                    }
//                                                }
//                                                "progress" -> {
//                                                    viewModelScope.launch(Dispatchers.Main) {
//                                                        updateProgress(event)
//                                                        if (event.subtopic != _currentStreamingSubtopic.value) {
//                                                            _currentStreamingText.value = ""
//                                                            _currentStreamingSubtopic.value = event.subtopic
//                                                        }
//                                                        _streamingEvents.tryEmit(event)
//                                                    }
//                                                }
//                                                "start" -> {
//                                                    viewModelScope.launch(Dispatchers.Main) {
//                                                        _streamingEvents.tryEmit(event)
//                                                    }
//                                                }
//                                                "complete" -> {
//                                                    viewModelScope.launch(Dispatchers.Main) {
//                                                        Log.d("STREAMING", "🎉 Streaming complete - starting polling")
//                                                        _isGeneratingContent.value = false
//                                                        _generationProgress.value = null
//                                                        _streamingEvents.tryEmit(event)
//                                                        startPollingFullContent(courseId)
//                                                    }
//                                                }
//                                                "error" -> {
//                                                    viewModelScope.launch(Dispatchers.Main) {
//                                                        Log.e("STREAMING", "❌ Stream error: ${event.message}")
//                                                        stopStreaming()
//                                                    }
//                                                }
//                                            }
//
//                                        } catch (e: Exception) {
//                                            Log.e("SSE", "❌ Parse error: $json", e)
//                                        }
//                                    }
//                                }
//                            }
//                        } catch (e: Exception) {
//                            Log.e("STREAMING", "Stream read error", e)
//                        } finally {
//                            response.close()
//                        }
//                    }
//                })
//            } catch (e: Exception) {
//                Log.e("STREAMING", "❌ SSE Failed: ${e.message}", e)
//                withContext(Dispatchers.Main) {
//                    _streamingEvents.tryEmit(SSEEvent(type = "error", message = e.message))
//                    stopStreaming()
//                }
//            }

        }
    }

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

//    // Update the generateCourseContent method
//    fun generateCourseContent(courseId: String, provider: String? = null, model: String? = null) {
//        viewModelScope.launch {
//            _isGeneratingContent.value = true
//            _generationStartTime.value = System.currentTimeMillis()
//            _content.value = Resource.Loading()
//            try {
//                val response = repository.generateContent(courseId, provider, model)
//                if (response.isSuccessful) {
//                    _content.value = Resource.Success(response.body()!!)
//                    // Start polling only AFTER content generation starts
//                    startPollingGenerationStatus(courseId)
//                } else {
//                    _content.value = Resource.Error("Failed to generate content: ${response.code()}")
//                    _isGeneratingContent.value = false
//                }
//            } catch (e: Exception) {
//                _content.value = Resource.Error(e.localizedMessage ?: "Unknown error")
//                _isGeneratingContent.value = false
//            }
//        }
//    }

    // Auto-polling function
//    private var pollingJob: Job? = null
//
//    fun stopPolling() {
//        pollingJob?.cancel()
//        pollingJob = null
//    }
//
//    // Update polling to handle initial delays
//    fun startPollingGenerationStatus(courseId: String, interval: Long = 3000L) {
//        pollingJob?.cancel()
//        pollingJob = viewModelScope.launch {
//            // Wait a bit before first poll to allow generation to start
//            delay(2000)
//            while (true) {
//                getGenerationStatus(courseId)
//                delay(interval)
//            }
//        }
//    }
//
//    fun getGenerationStatus(courseId: String, since: String? = null) {
//        viewModelScope.launch {
//            _generationStatus.value = Resource.Loading()
//            try {
//                val response = repository.getCourseGenerationStatus(courseId, since)
//                if (response.isSuccessful) {
//                    _generationStatus.value = Resource.Success(response.body()!!)
//                } else {
//                    _generationStatus.value = Resource.Error("Failed to get generation status: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                _generationStatus.value = Resource.Error(e.localizedMessage ?: "Unknown error")
//            }
//        }


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


//    fun getFullCourseContent(courseId: String) {
//        viewModelScope.launch {
//            // Preserve previous data during loading
//            val current = _fullCourseContent.value
//            val previousData = if (current is Resource.Success) current.data else null
//
//            _fullCourseContent.value = Resource.Loading(previousData)  // ← FIXED
//
//            try {
//                val response = repository.getFullCourse(courseId)
//                if (response.isSuccessful && response.body() != null) {
//                    _fullCourseContent.value = Resource.Success(response.body()!!)
//                } else {
//                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
//                    _fullCourseContent.value = Resource.Error(errorMsg, previousData)
//                }
//            } catch (e: Exception) {
//                _fullCourseContent.value = Resource.Error(
//                    message = e.localizedMessage ?: "Network error",
//                    data = previousData
//                )
//            }
//        }
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
            _enrolledCourses.value = Resource.Loading()
            try {
                val response = repository.getEnrolledCourses()
                if (response.isSuccessful) {
                    _enrolledCourses.value = Resource.Success(response.body()!!)
                } else {
                    _enrolledCourses.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _enrolledCourses.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _enroll.value = Resource.Loading()
            try {
                val response = repository.enrollInCourse(courseId)
                if (response.isSuccessful) {
                    _enroll.value = Resource.Success(response.body()!!)
                } else {
                    _enroll.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _enroll.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }


}
