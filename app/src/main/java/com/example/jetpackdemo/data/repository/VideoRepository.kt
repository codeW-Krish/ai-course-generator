package com.example.jetpackdemo.data.repository

import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.model.*
import retrofit2.Response

class VideoRepository(private val api: ApiService) {

    suspend fun generateManifest(
        subtopicId: String,
        llmProvider: String? = null,
        ttsProvider: String? = null,
        voice: String? = null,
        imageProvider: String? = null
    ): Response<GenerateManifestResponse> {
        return api.generateVideoManifest(subtopicId, llmProvider, ttsProvider, voice, imageProvider)
    }

    suspend fun getManifest(
        subtopicId: String
    ): Response<ManifestStatusResponse> {
        return api.getVideoManifest(subtopicId)
    }

    suspend fun regenerateManifest(
        subtopicId: String,
        llmProvider: String? = null,
        ttsProvider: String? = null,
        voice: String? = null,
        imageProvider: String? = null
    ): Response<GenerateManifestResponse> {
        return api.regenerateVideoManifest(subtopicId, llmProvider, ttsProvider, voice, imageProvider)
    }

    suspend fun getCourseManifests(
        courseId: String
    ): Response<CourseManifestsResponse> {
        return api.getCourseVideoManifests(courseId)
    }
}
