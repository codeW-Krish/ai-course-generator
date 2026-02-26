package com.example.jetpackdemo.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.AudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioViewModel(
    private val repository: AudioRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _audioState = MutableStateFlow<Resource<GetAudioResponse>>(Resource.Loading())
    val audioState: StateFlow<Resource<GetAudioResponse>> = _audioState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun loadSubtopicAudio(
        subtopicId: String,
        ttsProvider: String? = "Groq",
        voice: String? = "autumn",
        llmProvider: String? = "Groq"
    ) {
        viewModelScope.launch {
            _audioState.value = Resource.Loading()
            try {
                val response = repository.getSubtopicAudio(subtopicId, ttsProvider, voice, llmProvider)
                if (response.isSuccessful && response.body() != null) {
                    _audioState.value = Resource.Success(response.body()!!)
                    Log.d("AudioVM", "Audio loaded: ${response.body()!!.audio.audioUrl}")
                } else {
                    _audioState.value = Resource.Error("Failed to load audio: ${response.code()}")
                    Log.e("AudioVM", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("AudioVM", "Error loading audio", e)
                _audioState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadCourseAudio(
        courseId: String,
        ttsProvider: String? = "Groq",
        voice: String? = "autumn",
        llmProvider: String? = "Groq"
    ) {
        viewModelScope.launch {
            _audioState.value = Resource.Loading()
            try {
                val response = repository.getCourseAudio(courseId, ttsProvider, voice, llmProvider)
                if (response.isSuccessful && response.body() != null) {
                    _audioState.value = Resource.Success(response.body()!!)
                    Log.d("AudioVM", "Course audio loaded: ${response.body()!!.audio.audioUrl}")
                } else {
                    _audioState.value = Resource.Error("Failed to load course audio: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("AudioVM", "Error loading course audio", e)
                _audioState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }
}
