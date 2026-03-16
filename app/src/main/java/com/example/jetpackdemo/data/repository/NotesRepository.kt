package com.example.jetpackdemo.data.repository

import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.model.*
import retrofit2.Response

class NotesRepository(private val api: ApiService) {

    suspend fun getGeneratedNotes(
        subtopicId: String,
        provider: String? = null,
        model: String? = null
    ): Response<GetGeneratedNotesResponse> {
        return api.getGeneratedNotes(subtopicId, provider, model)
    }

    suspend fun exportSubtopicNotes(
        subtopicId: String,
        format: String = "json"
    ): Response<ExportSubtopicNotesResponse> {
        return api.exportSubtopicNotes(subtopicId, format)
    }

    suspend fun exportCourseNotes(
        courseId: String,
        format: String = "json"
    ): Response<ExportCourseNotesResponse> {
        return api.exportCourseNotes(courseId, format)
    }
}
