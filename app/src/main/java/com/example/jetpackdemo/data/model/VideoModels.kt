package com.example.jetpackdemo.data.model

import com.google.gson.annotations.SerializedName

// =====================================================================
// === Video Manifest Models (Client-Side Playback Engine) =============
// =====================================================================

// === Response wrapper for generate / get manifest ===

data class GenerateManifestResponse(
    val status: Int? = null,
    val manifest: VideoManifest? = null,
    val generated: Boolean = false,
    val error: String? = null
)

data class ManifestStatusResponse(
    val status: String = "not_generated",  // "completed" | "not_generated"
    val manifest: VideoManifest? = null
)

data class CourseManifestsResponse(
    @SerializedName("course_id")
    val courseId: String = "",
    @SerializedName("course_title")
    val courseTitle: String = "",
    val manifests: List<SubtopicManifestStatus> = emptyList()
)

data class SubtopicManifestStatus(
    @SerializedName("subtopic_id")
    val subtopicId: String = "",
    @SerializedName("subtopic_title")
    val subtopicTitle: String = "",
    @SerializedName("unit_title")
    val unitTitle: String = "",
    val status: String = "not_generated",
    val manifest: VideoManifest? = null
)

// === The core manifest — everything the player needs ===

data class VideoManifest(
    val id: String? = null,
    @SerializedName("manifest_version")
    val manifestVersion: Int = 1,
    @SerializedName("subtopic_id")
    val subtopicId: String = "",
    @SerializedName("subtopic_title")
    val subtopicTitle: String = "",
    @SerializedName("unit_title")
    val unitTitle: String = "",
    @SerializedName("course_title")
    val courseTitle: String = "",
    @SerializedName("course_id")
    val courseId: String = "",
    @SerializedName("total_duration_seconds")
    val totalDurationSeconds: Double = 0.0,
    @SerializedName("scene_count")
    val sceneCount: Int = 0,
    val resolution: VideoResolution? = null,
    @SerializedName("generated_at")
    val generatedAt: String? = null,
    val scenes: List<VideoScene> = emptyList()
)

data class VideoResolution(
    val width: Int = 1280,
    val height: Int = 720
)

// === Individual scene in the manifest ===

data class VideoScene(
    @SerializedName("scene_index")
    val sceneIndex: Int = 0,
    @SerializedName("scene_type")
    val sceneType: String = "",  // diagram, code, timeline, comparison, quote, illustration
    @SerializedName("visual_url")
    val visualUrl: String = "",
    @SerializedName("audio_url")
    val audioUrl: String = "",
    @SerializedName("duration_seconds")
    val durationSeconds: Double = 0.0,
    @SerializedName("key_concept")
    val keyConcept: String = "",
    @SerializedName("narration_text")
    val narrationText: String = "",
    @SerializedName("animation_style")
    val animationStyle: String = "fade",
    val transition: VideoTransition? = null,
    val subscenes: List<VideoSubscene> = emptyList()
)

// === Transition between scenes ===

data class VideoTransition(
    val type: String = "crossfade",  // crossfade, slide_left, zoom_morph, blur_dissolve, wipe, etc.
    @SerializedName("duration_ms")
    val durationMs: Int = 800
)

// === Subscene: visual change within a single audio chunk ===

data class VideoSubscene(
    val index: Int = 0,
    @SerializedName("start_ms")
    val startMs: Int = 0,
    @SerializedName("duration_ms")
    val durationMs: Int = 0,
    @SerializedName("animation_style")
    val animationStyle: String = "fade",
    @SerializedName("text_overlay")
    val textOverlay: String = ""
)
