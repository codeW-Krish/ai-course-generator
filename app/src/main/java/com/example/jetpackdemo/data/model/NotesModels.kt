package com.example.jetpackdemo.data.model

import com.google.gson.annotations.SerializedName

// === GET /api/notes/:subtopicId/generated ===
data class GetGeneratedNotesResponse(
    val notes: GeneratedNotes,
    val generated: Boolean
)

data class GeneratedNotes(
    val id: String? = null,
    @SerializedName("subtopic_id")
    val subtopicId: String? = null,
    @SerializedName("subtopic_title")
    val subtopicTitle: String? = null,
    @SerializedName("unit_title")
    val unitTitle: String? = null,
    @SerializedName("course_title")
    val courseTitle: String? = null,
    val difficulty: String? = null,
    val summary: String = "",
    @SerializedName("the_problem")
    val theProblem: String = "",
    @SerializedName("previous_approaches")
    val previousApproaches: String = "",
    @SerializedName("the_solution")
    val theSolution: String = "",
    @SerializedName("key_points")
    val keyPoints: List<String>? = emptyList(),
    val analogy: String = "",
    @SerializedName("real_world_example")
    val realWorldExample: String = "",
    @SerializedName("technical_example")
    val technicalExample: TechnicalExample? = null,
    val workflow: List<String>? = emptyList(),
    @SerializedName("common_mistakes")
    val commonMistakes: List<String>? = emptyList(),
    @SerializedName("common_confusions")
    val commonConfusions: List<String>? = emptyList(),
    @SerializedName("mini_qa")
    val miniQa: List<MiniQA>? = emptyList(),
    @SerializedName("generated_at")
    val generatedAt: String? = null
)

data class TechnicalExample(
    val language: String? = null,
    val code: String = "",
    val explanation: String = ""
)

data class MiniQA(
    val question: String = "",
    val answer: String = ""
)

// === GET /api/notes/:subtopicId/export (format=json) ===
data class ExportSubtopicNotesResponse(
    val notes: GeneratedNotes
)

// === GET /api/notes/course/:courseId/export (format=json) ===
data class ExportCourseNotesResponse(
    @SerializedName("course_title")
    val courseTitle: String,
    @SerializedName("total_subtopics")
    val totalSubtopics: Int,
    @SerializedName("notes_generated")
    val notesGenerated: Int
)
