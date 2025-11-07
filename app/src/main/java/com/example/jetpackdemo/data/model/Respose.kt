package com.example.jetpackdemo.data.model

// Search
data class SearchResponse(val courses: List<SearchItem>)
data class FullSearchResponse(val courses: List<FullSearchItem>)

data class SearchItem(
    val id: String,
    val title: String,
    val creator_name: String?
)

data class FullSearchItem(
    val id: String,
    val title: String,
    val description: String?,
    val difficulty: String?,
    val creator_name: String?,
    val total_users_joined: Int,
    val created_at: String
)

// Notes & Progress
data class NoteResponse(val note: String)
data class ProgressItem(val subtopic_id: String, val completed: Boolean)