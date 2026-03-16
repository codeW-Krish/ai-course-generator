package com.example.jetpackdemo.data.model

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

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
    val createdBy: String? = null,
    @SerializedName("include_videos")
    val includeVideos: Boolean = false,
    @SerializedName("outline_json")
    val outlineJson: Any? = null,
    @SerializedName("created_at")
    val createdAt: Any? = null,
    @SerializedName("outline_generated_at")
    val outlineGeneratedAt: Any? = null,
    val status: String? = null,
    @SerializedName("is_public")
    val isPublic: Boolean = true,
    @SerializedName("creator_name")
    val creatorName: String? = null,
    @SerializedName("total_users_joined")
    val totalUsersJoined: Int = 0
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
    @JsonAdapter(GeneratedSubtopicContentJsonAdapter::class)
    val content: GeneratedSubtopicContent? = null,
    val position: Int,
    @SerializedName("content_generated_at")
    val contentGeneratedAt: Any? = null,
    val videos: List<Video> = emptyList()
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

class GeneratedSubtopicContentJsonAdapter : JsonDeserializer<GeneratedSubtopicContent?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): GeneratedSubtopicContent? {
        if (json == null || json.isJsonNull) return null

        return try {
            when {
                json.isJsonObject -> parseObject(json.asJsonObject)
                json.isJsonArray -> parseArray(json.asJsonArray)
                json.isJsonPrimitive -> parsePrimitive(json.asString)
                else -> fallbackFromRaw(json.toString())
            }
        } catch (_: Exception) {
            fallbackFromRaw(json.toString())
        }
    }

    private fun parseArray(array: JsonArray): GeneratedSubtopicContent? {
        if (array.size() == 0) return null
        val first = array.firstOrNull { !it.isJsonNull } ?: return null
        return deserialize(first, null, null)
    }

    private fun parsePrimitive(raw: String): GeneratedSubtopicContent {
        val trimmed = raw.trim()
        val unwrapped = trimmed
            .removePrefix("\"")
            .removeSuffix("\"")
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\t", "\t")

        if ((unwrapped.startsWith("{") && unwrapped.endsWith("}")) ||
            (unwrapped.startsWith("[") && unwrapped.endsWith("]"))
        ) {
            return deserialize(JsonParser.parseString(unwrapped), null, null)
                ?: fallbackFromRaw(unwrapped)
        }
        return fallbackFromRaw(raw)
    }

    private fun parseObject(obj: JsonObject): GeneratedSubtopicContent {
        val nestedContent = obj.getString("content")?.trim()
        if (!nestedContent.isNullOrBlank()) {
            val parsedNested = runCatching { parsePrimitive(nestedContent) }.getOrNull()
            if (parsedNested != null &&
                (parsedNested.coreConcepts.isNotEmpty() ||
                        parsedNested.examples.isNotEmpty() ||
                        parsedNested.whyThisMatters.isNotBlank())
            ) {
                return parsedNested
            }
        }

        val subtopicTitle =
            obj.getString("subtopic_title")
                ?: obj.getString("subtopicTitle")
                ?: obj.getString("title")
                ?: ""

        val title =
            obj.getString("title")
                ?: obj.getString("subtopic_title")
                ?: "Learning Content"

        val whyThisMatters =
            obj.getString("why_this_matters")
                ?: obj.getString("whyThisMatters")
                ?: obj.getString("content")
                ?: ""

        val coreConcepts = obj.getAsJsonArray("core_concepts")
            ?.mapNotNull { element ->
                val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
                val concept = item.getString("concept") ?: return@mapNotNull null
                val explanation = item.getString("explanation") ?: ""
                CoreConcept(concept = concept, explanation = explanation)
            }
            ?: emptyList()

        val examples = obj.getAsJsonArray("examples")
            ?.mapNotNull { element ->
                val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
                val type = item.getString("type") ?: "example"
                val content = item.getString("content") ?: ""
                Example(type = type, content = content)
            }
            ?: emptyList()

        val codeOrMath =
            obj.getString("code_or_math")
                ?: obj.getString("codeOrMath")

        val youtubeKeywords = obj.getAsJsonArray("youtube_keywords")
            ?.mapNotNull { it.takeIf { value -> value.isJsonPrimitive }?.asString }
            ?: emptyList()

        return GeneratedSubtopicContent(
            subtopicTitle = subtopicTitle,
            title = title,
            whyThisMatters = whyThisMatters,
            coreConcepts = coreConcepts,
            examples = examples,
            codeOrMath = codeOrMath,
            youtubeKeywords = youtubeKeywords
        )
    }

    private fun fallbackFromRaw(raw: String): GeneratedSubtopicContent {
        val normalized = raw.trim().removePrefix("\"").removeSuffix("\"")
        return GeneratedSubtopicContent(
            subtopicTitle = "",
            title = "Learning Content",
            whyThisMatters = normalized,
            coreConcepts = emptyList(),
            examples = emptyList(),
            codeOrMath = null,
            youtubeKeywords = emptyList()
        )
    }

    private fun JsonObject.getString(key: String): String? {
        val value = get(key) ?: return null
        if (value.isJsonNull) return null
        return runCatching { value.asString }.getOrNull()
    }
}

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
