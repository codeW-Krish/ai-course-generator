package com.example.jetpackdemo.data.repository

import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.model.*
import retrofit2.Response

class FlashcardRepository(private val api: ApiService) {

    suspend fun getFlashcards(
        subtopicId: String,
        provider: String? = null,
        model: String? = null
    ): Response<GetFlashcardsResponse> {
        return api.getFlashcards(subtopicId, provider, model)
    }

    suspend fun reviewFlashcard(
        flashcardId: String,
        quality: Int
    ): Response<ReviewFlashcardResponse> {
        return api.reviewFlashcard(flashcardId, ReviewFlashcardRequest(quality))
    }

    suspend fun getDueFlashcards(courseId: String): Response<GetDueFlashcardsResponse> {
        return api.getDueFlashcards(courseId)
    }
}
