package com.example.jetpackdemo.data.model

import com.google.gson.annotations.SerializedName

// --- Request Body for Outline Generation ---
data class GenerateOutlineRequest(
    @SerializedName("course_title")
    val title: String,
    val description: String,
    @SerializedName("num_units")
    val numUnits: Int,
    val difficulty: String,
    @SerializedName("include_youtube")
    val includeVideos: Boolean,
    val provider: String = "Gemini",
    val model: String? = null
)

// --- Response Body from Outline Generation ---
data class GenerateOutlineResponse(
    val courseId: String,
    val status: String,
    val outline: CourseOutline
)

data class CourseOutline(
    @SerializedName("course_title")
    val courseTitle: String,
    val difficulty: String?,
    val units: List<OutlineUnit>
)

data class OutlineUnit(
    val position: Int,
    val title: String,
    val subtopics: List<String>
)

// --- Course Management ---
data class CoursesResponse(
    val courses: List<Course>
)

data class enrolledCoursesResponse(
    @SerializedName("enrolledCourses")
    val courses: List<Course>
)
data class MyCoursesResponse(
    @SerializedName("myCourses")
    val courses: List<Course>
)
data class Course(
    val id: String,
    val title: String,
    val description: String?,
    val difficulty: String?,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("include_videos")
    val includeVideos: Boolean,
    @SerializedName("outline_json")
    val outlineJson: Any?, // can map to CourseOutline if always same shape
    @SerializedName("created_at")
    val createdAt: String
)

data class EnrollCourseRequest(
    @SerializedName("course_id")
    val courseId: String
)

data class EnrollResponse(
    val message: String
)
// Update your data models to include videos
data class CourseFullResponse(
    val course: Course,
    val units: List<UnitWithSubtopics>
)

data class UnitWithSubtopics(
    val id: String,
    @SerializedName("course_id")
    val courseId: String,
    val title: String,
    val position: Int,
    val subtopics: List<Subtopic> // This now includes videos
)

data class Subtopic(
    val id: String,
    @SerializedName("unit_id")
    val unitId: String,
    val title: String,
    val content: String?,
    val position: Int,
    val contentGeneratedAt: String?,
    val videos: List<Video> = emptyList() // Add this field
)

// Add Video data class
data class Video(
    val id: String,
    @SerializedName("subtopic_id")
    val subtopicId: String,
    val title: String,
    @SerializedName("youtube_url")
    val youtubeUrl: String,
    val thumbnail: String,
    @SerializedName("duration_sec")
    val durationSec: Int?
)
// --- Generated Subtopic Content (matches SubtopicContentSchema) ---
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
    val codeOrMath: String?, // nullable

    @SerializedName("youtube_keywords")
    val youtubeKeywords: List<String>?
)

data class CoreConcept(
    val concept: String,
    val explanation: String
)

data class Example(
    val type: String, // "analogy" or "technical_example"
    val content: String
)

// --- Subtopic Batch Response (array of GeneratedSubtopicContent) ---
typealias SubtopicBatchResponse = List<GeneratedSubtopicContent>

// --- Content Generation Status ---
data class ContentGenerationStatusResponse(
    val message: String,
    val units: Int,
    @SerializedName("remaining_subtopics")
    val remainingSubtopics: Int,
    val status: String // "in_progress", "completed", etc.
)

// --- Background Generation Status (detailed tracking) ---
data class GenerationStatusResponse(
    val courseId: String,
    val status: String,
    val totalSubtopics: Int?,
    val generatedSubtopics: Int,
    val lastUpdated: String,
    val subtopics: List<GeneratedSubtopic>
)

data class GeneratedSubtopic(
    val id: String,
    val title: String,
    val content: GeneratedSubtopicContent,
    @SerializedName("content_generated_at")
    val contentGeneratedAt: String,
    @SerializedName("unit_title")
    val unitTitle: String
)
