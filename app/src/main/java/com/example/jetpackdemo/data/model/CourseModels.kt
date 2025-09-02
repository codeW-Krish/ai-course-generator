package com.example.jetpackdemo.data.model

import com.google.gson.annotations.SerializedName

// --- Request Body for Outline Generation ---
// This class matches the JSON request body that the app sends to the backend
data class GenerateOutlineRequest(
    @SerializedName("course_title")
    val title: String,
    val description: String,
    @SerializedName("num_units")
    val numUnits: Int,
    val difficulty: String,
    @SerializedName("include_youtube")
    val includeVideos: Boolean
)

// --- Response Body from Outline Generation ---
// These classes match the JSON the backend sends back to our app

data class GenerateOutlineResponse(
    val courseId: String,
    val status: String,
    val outline: CourseOutline
)

data class CourseOutline(
    @SerializedName("course_title")
    val courseTitle: String,
    val difficulty: String,
    val units: List<OutlineUnit>
)

data class OutlineUnit(
    val position: Int,
    val title: String,
    val subtopics: List<String>
)

data class CoursesResponse(
    val courses: List<Course>
)

data class Course(
    val id: String,
    val title: String,
    val description: String?,
    val difficulty: String?,
    val created_by: String,
    val include_videos: Boolean,
    val outline_json: Any?, // or a custom type
    val created_at: String
)

data class EnrollResponse(
    val message: String
)

data class CourseFullResponse(
    val course: Course,
    val units: List<UnitWithSubtopics>
)

data class UnitWithSubtopics(
    val id: String,
    val course_id: String,
    val title: String,
    val position: Int,
    val subtopics: List<Subtopic>
)

data class Subtopic(
    val id: String,
    val unit_id: String,
    val title: String,
    val content: String?,
    val position: Int
)

data class GeneratedSubtopicContent(
    @SerializedName("subtopic_title")
    val subtopicTitle: String,

    val title: String,

    @SerializedName("why_this_matters")
    val whyThisMatters: String,

    @SerializedName("core_concepts")
    val coreConcepts: List<CoreConcept>,

    val examples: List<Example>,

    @SerializedName("code_or_math")
    val codeOrMath: String?, // nullable, can be null

    @SerializedName("youtube_keywords")
    val youtubeKeywords: List<String>
)

data class CoreConcept(
    val concept: String,
    val explanation: String
)

data class Example(
    val type: String, // e.g. "analogy", "technical_example"
    val content: String
)

data class GenerationStatusResponse(
    val courseId: String,
    val status: String,               // e.g., "failed", "completed"
    val totalSubtopics: Int?,         // nullable
    val generatedSubtopics: Int,
    val lastUpdated: String,           // ISO timestamp
    val subtopics: List<GeneratedSubtopic>  // list of generated subtopic details
)

data class GeneratedSubtopic(
    val id: String,
    val title: String,
    val content: GeneratedSubtopicContent,  // nested JSON object (not a String)
    val content_generated_at: String,
    val unit_title: String
)

