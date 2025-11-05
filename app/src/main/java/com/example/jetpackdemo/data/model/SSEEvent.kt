package com.example.jetpackdemo.data.model

import kotlinx.serialization.Serializable

// In data/model/SSEEvent.kt
@Serializable
// In your SSEEvent.kt or inside ViewModel
data class SSEEvent(
    val type: String,
    val subtopic: String? = null,
    val unit: String? = null,
    val progress: Int? = null,
    val generated: Int? = null,
    val total: Int? = null,
    val message: String? = null,
    val chunk: String? = null  // ← NEW
)