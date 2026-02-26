package com.example.jetpackdemo.data.repository

import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.model.*
import retrofit2.Response

class InteractiveRepository(private val api: ApiService) {

    // === Legacy endpoints (still used for direct subtopic access) ===

    suspend fun getNextSubtopic(
        courseId: String,
        provider: String = "Groq"
    ): Response<InteractiveSessionResponse> {
        return api.getNextInteractiveSubtopic(courseId, provider)
    }

    suspend fun getSession(
        subtopicId: String,
        provider: String = "Groq"
    ): Response<InteractiveSessionResponse> {
        return api.getInteractiveSession(subtopicId, provider)
    }

    suspend fun verifyAnswer(
        subtopicId: String,
        questionId: String,
        answer: String
    ): Response<VerifyAnswerResponse> {
        return api.verifyAnswer(subtopicId, VerifyAnswerRequest(questionId, answer))
    }

    suspend fun sendChat(
        subtopicId: String,
        message: String,
        provider: String = "Groq"
    ): Response<InteractiveChatResponse> {
        return api.sendInteractiveChat(subtopicId, InteractiveChatRequest(message, provider))
    }

    suspend fun chatWithCourseAI(
        courseId: String,
        message: String,
        provider: String = "Groq",
        sessionId: String? = null
    ): Response<CourseChatResponse> {
        return api.chatWithCourseAI(courseId, CourseChatRequest(message, provider, sessionId))
    }

    suspend fun generateCoursePractice(
        courseId: String,
        focus: String = "general revision",
        provider: String = "Groq"
    ): Response<CoursePracticeResponse> {
        return api.generateCoursePractice(courseId, CoursePracticeRequest(focus, provider))
    }

    // === Content-First Flow endpoints ===

    suspend fun getNextContent(
        courseId: String,
        provider: String = "Groq"
    ): Response<ContentResponse> {
        return api.getNextContent(courseId, provider)
    }

    suspend fun getQuiz(
        subtopicId: String,
        provider: String = "Groq"
    ): Response<QuizResponse> {
        return api.getQuiz(subtopicId, provider)
    }

    suspend fun submitQuiz(
        subtopicId: String,
        answers: List<QuizAnswerItem>
    ): Response<SubmitQuizResponse> {
        return api.submitQuiz(subtopicId, SubmitQuizRequest(answers))
    }

    // === Hub Activity ===

    suspend fun logHubActivity(
        courseId: String,
        subtopicId: String? = null,
        featureType: String,
        title: String = ""
    ): Response<HubActivity> {
        return api.logHubActivity(HubActivityRequest(courseId, subtopicId, featureType, title))
    }

    suspend fun getHubHistory(
        courseId: String,
        limit: Int = 50
    ): Response<HubHistoryResponse> {
        return api.getHubHistory(courseId, limit)
    }
}
