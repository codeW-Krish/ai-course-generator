package com.example.jetpackdemo.data.model

import com.google.gson.annotations.SerializedName

// === Interactive Session (GET /api/interactive/:subtopicId or /course/:courseId/next) ===
data class InteractiveSessionResponse(
    val subtopic: InteractiveSubtopic? = null,
    val questions: List<InteractiveQuestion> = emptyList(),
    @SerializedName("hearts_remaining")
    val heartsRemaining: Int = 3,
    val attempts: Int = 0,
    @SerializedName("is_completed")
    val isCompleted: Boolean = false,
    @SerializedName("course_completed")
    val courseCompleted: Boolean = false,
    val message: String? = null
)

data class InteractiveSubtopic(
    val id: String,
    val title: String,
    val content: Any? = null,
    @SerializedName("course_id")
    val courseId: String? = null
)

data class InteractiveQuestion(
    val id: String,
    @SerializedName("question_text")
    val questionText: String,
    val options: List<String> = emptyList(),
    val hint: String? = null,
    val type: String = "mcq"
)

// === Verify Answer (POST /api/interactive/:subtopicId/verify) ===
data class VerifyAnswerRequest(
    val questionId: String,
    val answer: String
)

data class VerifyAnswerResponse(
    val correct: Boolean,
    @SerializedName("correct_answer")
    val correctAnswer: String? = null,
    val hint: String? = null,
    @SerializedName("hearts_remaining")
    val heartsRemaining: Int = 0,
    val result: String? = null,
    @SerializedName("game_over")
    val gameOver: Boolean = false,
    @SerializedName("is_subtopic_completed")
    val isSubtopicCompleted: Boolean = false
)

// === Chat (POST /api/interactive/:subtopicId/chat) ===
data class InteractiveChatRequest(
    val message: String,
    val provider: String = "Groq"
)

data class InteractiveChatResponse(
    @SerializedName("ai_response")
    val aiResponse: String
)

// === Course Chat (POST /api/interactive/course/:courseId/chat) ===
data class CourseChatRequest(
    val message: String,
    val provider: String = "Groq",
    @SerializedName("session_id")
    val sessionId: String? = null
)

data class CourseChatResponse(
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("ai_response")
    val aiResponse: String,
    val provider: String? = null
)

// === Course Practice (POST /api/interactive/course/:courseId/practice) ===
data class CoursePracticeRequest(
    val focus: String = "general revision",
    val provider: String = "Groq"
)

data class CoursePracticeResponse(
    @SerializedName("course_id")
    val courseId: String,
    val focus: String,
    val provider: String,
    val questions: List<PracticeQuestion> = emptyList()
)

data class PracticeQuestion(
    val question: String,
    val answer: String,
    val explanation: String = "",
    val type: String = "concept"
)

// === Audio (GET /api/audio/:subtopicId, /api/audio/course/:courseId) ===
data class GetAudioResponse(
    val audio: AudioData,
    val generated: Boolean
)

data class AudioData(
    val id: String? = null,
    val type: String = "subtopic",
    @SerializedName("subtopic_id")
    val subtopicId: String? = null,
    @SerializedName("subtopic_title")
    val subtopicTitle: String? = null,
    @SerializedName("unit_title")
    val unitTitle: String? = null,
    @SerializedName("course_title")
    val courseTitle: String? = null,
    @SerializedName("course_id")
    val courseId: String? = null,
    val script: String = "",
    @SerializedName("segment_count")
    val segmentCount: Int = 0,
    @SerializedName("estimated_duration")
    val estimatedDuration: Int = 0,
    @SerializedName("tts_provider")
    val ttsProvider: String? = null,
    val voice: String? = null,
    @SerializedName("audio_url")
    val audioUrl: String = "",
    @SerializedName("imagekit_file_id")
    val imagekitFileId: String? = null,
    @SerializedName("file_size_bytes")
    val fileSizeBytes: Long = 0,
    @SerializedName("generated_at")
    val generatedAt: String? = null
)

// === Chat message for local UI state ===
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// =====================================================================
// === Content-First Interactive Flow Models ============================
// =====================================================================

// GET /api/interactive/course/:courseId/next-content
data class ContentResponse(
    @SerializedName("course_completed")
    val courseCompleted: Boolean = false,
    val subtopic: InteractiveSubtopic? = null,
    @SerializedName("questions_ready")
    val questionsReady: Boolean = false,
    @SerializedName("hearts_remaining")
    val heartsRemaining: Int = 3,
    val attempts: Int = 0,
    @SerializedName("is_completed")
    val isCompleted: Boolean = false,
    @SerializedName("course_progress")
    val courseProgress: CourseProgress? = null,
    val message: String? = null
)

data class CourseProgress(
    val completed: Int = 0,
    val total: Int = 0
)

// GET /api/interactive/:subtopicId/quiz
data class QuizResponse(
    @SerializedName("subtopic_id")
    val subtopicId: String,
    val questions: List<QuizQuestion> = emptyList(),
    @SerializedName("hearts_remaining")
    val heartsRemaining: Int = 3,
    @SerializedName("is_completed")
    val isCompleted: Boolean = false
)

data class QuizQuestion(
    val id: String,
    @SerializedName("question_text")
    val questionText: String,
    @SerializedName("question_type")
    val questionType: String = "mcq",
    val options: List<String> = emptyList(),
    @SerializedName("correct_answer")
    val correctAnswer: String,
    val hint: String = "",
    val explanation: String = "",
    val position: Int = 0
)

// POST /api/interactive/:subtopicId/submit-quiz
data class SubmitQuizRequest(
    val answers: List<QuizAnswerItem>
)

data class QuizAnswerItem(
    @SerializedName("question_id")
    val questionId: String,
    @SerializedName("user_answer")
    val userAnswer: String,
    @SerializedName("is_correct")
    val isCorrect: Boolean
)

data class SubmitQuizResponse(
    @SerializedName("subtopic_id")
    val subtopicId: String,
    @SerializedName("total_questions")
    val totalQuestions: Int,
    @SerializedName("correct_count")
    val correctCount: Int,
    @SerializedName("wrong_count")
    val wrongCount: Int,
    @SerializedName("hearts_remaining")
    val heartsRemaining: Int,
    @SerializedName("is_completed")
    val isCompleted: Boolean,
    @SerializedName("is_perfect")
    val isPerfect: Boolean,
    @SerializedName("xp_earned")
    val xpEarned: Int
)

// === Hub Activity Models ===

// POST /api/interactive/hub/log
data class HubActivityRequest(
    @SerializedName("course_id")
    val courseId: String,
    @SerializedName("subtopic_id")
    val subtopicId: String? = null,
    @SerializedName("feature_type")
    val featureType: String,
    val title: String = ""
)

// GET /api/interactive/hub/history/:courseId
data class HubHistoryResponse(
    @SerializedName("course_id")
    val courseId: String,
    val activities: List<HubActivity> = emptyList(),
    @SerializedName("subtopic_statuses")
    val subtopicStatuses: Map<String, SubtopicFeatureStatus> = emptyMap(),
    @SerializedName("generated_items")
    val generatedItems: List<GeneratedItem> = emptyList()
)

data class HubActivity(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("course_id")
    val courseId: String,
    @SerializedName("subtopic_id")
    val subtopicId: String? = null,
    @SerializedName("feature_type")
    val featureType: String,
    val title: String = "",
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class SubtopicFeatureStatus(
    val title: String = "",
    @SerializedName("unit_title")
    val unitTitle: String? = null,
    @SerializedName("has_flashcards")
    val hasFlashcards: Boolean = false,
    @SerializedName("has_notes")
    val hasNotes: Boolean = false,
    @SerializedName("has_audio")
    val hasAudio: Boolean = false,
    @SerializedName("has_content")
    val hasContent: Boolean = false
)

data class GeneratedItem(
    val type: String = "",
    @SerializedName("subtopic_id")
    val subtopicId: String? = null,
    @SerializedName("subtopic_title")
    val subtopicTitle: String = "",
    @SerializedName("unit_title")
    val unitTitle: String? = null,
    @SerializedName("course_id")
    val courseId: String? = null,
    @SerializedName("generated_at")
    val generatedAt: String? = null,
    @SerializedName("audio_url")
    val audioUrl: String? = null,
    @SerializedName("estimated_duration")
    val estimatedDuration: Int = 0,
    @SerializedName("card_count")
    val cardCount: Int = 0
)

// === Local quiz state for client-side verification ===
data class QuizAttemptResult(
    val questionId: String,
    val questionText: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val explanation: String = "",
    val hint: String = ""
)
