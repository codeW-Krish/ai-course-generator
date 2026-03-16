package com.example.jetpackdemo.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoViewModel(
    private val repository: VideoRepository,
    application: Application
) : AndroidViewModel(application) {

    // --- Manifest loading state ---
    private val _manifestState = MutableStateFlow<Resource<VideoManifest>>(Resource.Loading())
    val manifestState: StateFlow<Resource<VideoManifest>> = _manifestState.asStateFlow()

    // --- Playback state ---
    private val _currentSceneIndex = MutableStateFlow(0)
    val currentSceneIndex: StateFlow<Int> = _currentSceneIndex.asStateFlow()

    private val _currentSubsceneIndex = MutableStateFlow(0)
    val currentSubsceneIndex: StateFlow<Int> = _currentSubsceneIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f) // 0.0 to 1.0 overall
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _sceneProgress = MutableStateFlow(0f) // 0.0 to 1.0 within current scene
    val sceneProgress: StateFlow<Float> = _sceneProgress.asStateFlow()

    // Track elapsed time per scene for subscene switching
    private val _sceneElapsedMs = MutableStateFlow(0L)
    val sceneElapsedMs: StateFlow<Long> = _sceneElapsedMs.asStateFlow()

    // Subscene timer job
    private var subsceneTimerJob: Job? = null

    // --- Load or generate manifest ---

    fun loadManifest(subtopicId: String) {
        viewModelScope.launch {
            _manifestState.value = Resource.Loading()
            try {
                // First check if manifest is already generated
                val statusResponse = repository.getManifest(subtopicId)
                if (statusResponse.isSuccessful && statusResponse.body()?.status == "completed") {
                    val manifest = statusResponse.body()!!.manifest!!
                    _manifestState.value = Resource.Success(manifest)
                    Log.d("VideoVM", "Manifest loaded from cache: ${manifest.sceneCount} scenes")
                    return@launch
                }

                // Not generated yet — trigger generation
                _manifestState.value = Resource.Loading()
                val genResponse = repository.generateManifest(subtopicId)
                if (genResponse.isSuccessful && genResponse.body()?.manifest != null) {
                    val manifest = genResponse.body()!!.manifest!!
                    _manifestState.value = Resource.Success(manifest)
                    Log.d("VideoVM", "Manifest generated: ${manifest.sceneCount} scenes, ${manifest.totalDurationSeconds}s")
                } else {
                    val errorMsg = genResponse.body()?.error ?: genResponse.errorBody()?.string() ?: "Unknown error"
                    _manifestState.value = Resource.Error("Failed to generate video: $errorMsg")
                    Log.e("VideoVM", "Generation failed: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("VideoVM", "Error loading manifest", e)
                _manifestState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun regenerateManifest(subtopicId: String) {
        viewModelScope.launch {
            _manifestState.value = Resource.Loading()
            try {
                val response = repository.regenerateManifest(subtopicId)
                if (response.isSuccessful && response.body()?.manifest != null) {
                    val manifest = response.body()!!.manifest!!
                    _manifestState.value = Resource.Success(manifest)
                    resetPlayback()
                } else {
                    _manifestState.value = Resource.Error("Regeneration failed")
                }
            } catch (e: Exception) {
                _manifestState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // --- Playback controls ---

    fun play() {
        _isPlaying.value = true
        startSubsceneTimer()
    }

    fun pause() {
        _isPlaying.value = false
        subsceneTimerJob?.cancel()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pause() else play()
    }

    fun seekToScene(index: Int) {
        val manifest = (_manifestState.value as? Resource.Success)?.data ?: return
        if (index in manifest.scenes.indices) {
            _currentSceneIndex.value = index
            _currentSubsceneIndex.value = 0
            _sceneElapsedMs.value = 0L
            updateOverallProgress()
        }
    }

    fun nextScene() {
        val manifest = (_manifestState.value as? Resource.Success)?.data ?: return
        val next = _currentSceneIndex.value + 1
        if (next < manifest.scenes.size) {
            seekToScene(next)
        } else {
            // Playback complete
            pause()
        }
    }

    fun previousScene() {
        val prev = _currentSceneIndex.value - 1
        if (prev >= 0) {
            seekToScene(prev)
        }
    }

    /**
     * Called by ExoPlayer progress callback to sync subscene timing.
     * The audio drives the timeline — this method is called frequently
     * with the current playback position within the scene's audio.
     */
    fun onAudioProgress(positionMs: Long, durationMs: Long) {
        _sceneElapsedMs.value = positionMs

        // Update scene progress
        if (durationMs > 0) {
            _sceneProgress.value = (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        }

        // Determine which subscene should be active
        val manifest = (_manifestState.value as? Resource.Success)?.data ?: return
        val scene = manifest.scenes.getOrNull(_currentSceneIndex.value) ?: return
        val subscenes = scene.subscenes

        if (subscenes.size > 1) {
            for (i in subscenes.indices.reversed()) {
                if (positionMs >= subscenes[i].startMs) {
                    if (_currentSubsceneIndex.value != i) {
                        _currentSubsceneIndex.value = i
                    }
                    break
                }
            }
        }

        updateOverallProgress()
    }

    /**
     * Called when ExoPlayer finishes a scene's audio.
     * Auto-advances to next scene.
     */
    fun onSceneAudioComplete() {
        nextScene()
    }

    private fun resetPlayback() {
        _currentSceneIndex.value = 0
        _currentSubsceneIndex.value = 0
        _isPlaying.value = false
        _playbackProgress.value = 0f
        _sceneProgress.value = 0f
        _sceneElapsedMs.value = 0L
        subsceneTimerJob?.cancel()
    }

    private fun updateOverallProgress() {
        val manifest = (_manifestState.value as? Resource.Success)?.data ?: return
        val totalDuration = manifest.totalDurationSeconds
        if (totalDuration <= 0) return

        var elapsed = 0.0
        for (i in 0 until _currentSceneIndex.value) {
            elapsed += manifest.scenes[i].durationSeconds
        }
        elapsed += (_sceneProgress.value * (manifest.scenes.getOrNull(_currentSceneIndex.value)?.durationSeconds ?: 0.0))

        _playbackProgress.value = (elapsed / totalDuration).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Fallback timer for subscene switching if ExoPlayer callbacks
     * aren't providing fine enough timing.
     */
    private fun startSubsceneTimer() {
        subsceneTimerJob?.cancel()
        subsceneTimerJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(100) // 100ms tick
                // The actual subscene advancement is driven by onAudioProgress
                // This is just a safety net
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        subsceneTimerJob?.cancel()
    }
}
