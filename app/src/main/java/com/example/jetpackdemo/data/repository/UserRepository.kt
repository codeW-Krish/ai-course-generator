package com.example.jetpackdemo.data.repository

import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.model.*
import retrofit2.Response

class UserRepository(private val api: ApiService) {

    suspend fun getMyProfile(): Response<UserProfile> {
        return api.getMyProfile()
    }

    suspend fun updateMyProfile(request: UpdateProfileRequest): Response<UserProfile> {
        return api.updateMyProfile(request)
    }

    suspend fun getUserProfile(userId: String): Response<UserProfile> {
        return api.getUserProfile(userId)
    }

    suspend fun followUser(userId: String): Response<FollowResponse> {
        return api.followUser(userId)
    }

    suspend fun unfollowUser(userId: String): Response<FollowResponse> {
        return api.unfollowUser(userId)
    }

    suspend fun getFollowers(userId: String): Response<FollowersResponse> {
        return api.getFollowers(userId)
    }

    suspend fun getFollowing(userId: String): Response<FollowingResponse> {
        return api.getFollowing(userId)
    }
}
