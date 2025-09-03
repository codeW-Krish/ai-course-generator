package com.example.jetpackdemo.data.repository

import android.content.Context
import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.shared_pref.ProviderManager

class CourseRepository(private val api: ApiService, private val context: Context) {

    // Generate Outline
    suspend fun generateOutline(
        title: String,
        description: String,
        numUnits: Int,
        difficulty: String,
        includeVideos: Boolean
    ): GenerateOutlineResponse? {
        val provider = ProviderManager.getProvider(context)
        val model = ProviderManager.getModel(context)

        val request = GenerateOutlineRequest(
            title = title,
            description = description,
            numUnits = numUnits,
            difficulty = difficulty,
            includeVideos = includeVideos,
            provider = provider,
            model = model
        )

        val response = api.generateOutline(request)
        return if (response.isSuccessful) response.body() else null
    }

    // Generate Content
    suspend fun generateContent(courseId: String): GeneratedSubtopicContent? {
        val response = api.generateContent(courseId)
        return if (response.isSuccessful) response.body() else null
    }

    // Get Generation Status
    suspend fun getGenerationStatus(courseId: String, since: String? = null): GenerationStatusResponse? {
        val response = api.getCourseGenerationStatus(courseId, since)
        return if (response.isSuccessful) response.body() else null
    }
}
