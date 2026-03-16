package com.example.jetpackdemo.data.model

import com.google.gson.annotations.SerializedName

// === GET /api/flashcards/:subtopicId ===
data class GetFlashcardsResponse(
    val flashcards: List<FlashcardItem>,
    val generated: Boolean
)

data class FlashcardItem(
    val id: String,
    @SerializedName("subtopic_id")
    val subtopicId: String? = null,
    val front: String,
    val back: String,
    @SerializedName("card_type")
    val cardType: String = "concept",
    val position: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val progress: FlashcardProgress? = null
)

data class FlashcardProgress(
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("flashcard_id")
    val flashcardId: String? = null,
    @SerializedName("ease_factor")
    val easeFactor: Double = 2.5,
    @SerializedName("interval_days")
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    @SerializedName("next_review_at")
    val nextReviewAt: String? = null,
    @SerializedName("last_reviewed_at")
    val lastReviewedAt: String? = null
)

// === POST /api/flashcards/:flashcardId/review ===
data class ReviewFlashcardRequest(
    val quality: Int // 0-5
)

data class ReviewFlashcardResponse(
    val message: String,
    @SerializedName("next_review_at")
    val nextReviewAt: String,
    @SerializedName("interval_days")
    val intervalDays: Int,
    @SerializedName("ease_factor")
    val easeFactor: Double,
    val repetitions: Int
)

// === GET /api/flashcards/course/:courseId/due ===
data class GetDueFlashcardsResponse(
    val dueCards: List<FlashcardItem>,
    val total: Int
)
