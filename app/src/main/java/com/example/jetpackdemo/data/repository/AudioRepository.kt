package com.example.jetpackdemo.data.repository

import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.model.*
import retrofit2.Response

class AudioRepository(private val api: ApiService) {

    suspend fun getSubtopicAudio(
        subtopicId: String,
        ttsProvider: String? = null,
        voice: String? = null,
        llmProvider: String? = null,
        llmModel: String? = null
    ): Response<GetAudioResponse> {
        return api.getSubtopicAudio(subtopicId, ttsProvider, voice, llmProvider, llmModel)
    }

    suspend fun getCourseAudio(
        courseId: String,
        ttsProvider: String? = null,
        voice: String? = null,
        llmProvider: String? = null,
        llmModel: String? = null
    ): Response<GetAudioResponse> {
        return api.getCourseAudio(courseId, ttsProvider, voice, llmProvider, llmModel)
    }
}
