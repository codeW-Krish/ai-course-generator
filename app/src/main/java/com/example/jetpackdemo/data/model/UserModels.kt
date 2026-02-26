package com.example.jetpackdemo.data.model

import com.google.gson.annotations.SerializedName

// --- User Profile ---
data class UserProfile(
    val id: String,
    val username: String,
    val email: String? = null,
    val role: String? = "user",
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    val bio: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("followers_count")
    val followersCount: Int = 0,
    @SerializedName("following_count")
    val followingCount: Int = 0,
    @SerializedName("courses_count")
    val coursesCount: Int = 0,
    val courses: List<UserCoursePreview>? = null,
    @SerializedName("is_following")
    val isFollowing: Boolean = false,
    @SerializedName("is_own_profile")
    val isOwnProfile: Boolean = false
)

data class UserCoursePreview(
    val id: String,
    val title: String,
    val description: String? = null,
    val difficulty: String? = null,
    val status: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

// --- Follow ---
data class FollowResponse(
    val message: String
)

data class FollowersResponse(
    val followers: List<FollowUser>,
    val count: Int
)

data class FollowingResponse(
    val following: List<FollowUser>,
    val count: Int
)

data class FollowUser(
    val id: String,
    val username: String,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerializedName("is_following")
    val isFollowing: Boolean = false
)

// --- Profile Update ---
data class UpdateProfileRequest(
    val username: String? = null,
    val bio: String? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null
)
