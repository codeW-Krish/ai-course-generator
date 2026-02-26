package com.example.jetpackdemo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository,
    application: Application
) : AndroidViewModel(application) {

    // My profile
    private val _myProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading())
    val myProfile: StateFlow<Resource<UserProfile>> = _myProfile.asStateFlow()

    // Other user's profile
    private val _userProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading())
    val userProfile: StateFlow<Resource<UserProfile>> = _userProfile.asStateFlow()

    // Followers
    private val _followers = MutableStateFlow<Resource<FollowersResponse>>(Resource.Loading())
    val followers: StateFlow<Resource<FollowersResponse>> = _followers.asStateFlow()

    // Following
    private val _following = MutableStateFlow<Resource<FollowingResponse>>(Resource.Loading())
    val following: StateFlow<Resource<FollowingResponse>> = _following.asStateFlow()

    // Follow/unfollow action
    private val _followAction = MutableStateFlow<Resource<FollowResponse>?>(null)
    val followAction: StateFlow<Resource<FollowResponse>?> = _followAction.asStateFlow()

    // Profile update
    private val _profileUpdate = MutableStateFlow<Resource<UserProfile>?>(null)
    val profileUpdate: StateFlow<Resource<UserProfile>?> = _profileUpdate.asStateFlow()

    fun loadMyProfile() {
        viewModelScope.launch {
            _myProfile.value = Resource.Loading()
            try {
                val response = repository.getMyProfile()
                if (response.isSuccessful && response.body() != null) {
                    _myProfile.value = Resource.Success(response.body()!!)
                } else {
                    _myProfile.value = Resource.Error(response.message() ?: "Failed to load profile")
                }
            } catch (e: Exception) {
                _myProfile.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _userProfile.value = Resource.Loading()
            try {
                val response = repository.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    _userProfile.value = Resource.Success(response.body()!!)
                } else {
                    _userProfile.value = Resource.Error(response.message() ?: "Failed to load profile")
                }
            } catch (e: Exception) {
                _userProfile.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }

    fun updateMyProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            _profileUpdate.value = Resource.Loading()
            try {
                val response = repository.updateMyProfile(request)
                if (response.isSuccessful && response.body() != null) {
                    _profileUpdate.value = Resource.Success(response.body()!!)
                    // Refresh my profile
                    _myProfile.value = Resource.Success(response.body()!!)
                } else {
                    _profileUpdate.value = Resource.Error(response.message() ?: "Failed to update")
                }
            } catch (e: Exception) {
                _profileUpdate.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            _followAction.value = Resource.Loading()
            try {
                val response = repository.followUser(userId)
                if (response.isSuccessful && response.body() != null) {
                    _followAction.value = Resource.Success(response.body()!!)
                    // Update the profile to reflect new follow state
                    val currentProfile = _userProfile.value
                    if (currentProfile is Resource.Success && currentProfile.data != null) {
                        _userProfile.value = Resource.Success(
                            currentProfile.data.copy(
                                isFollowing = true,
                                followersCount = currentProfile.data.followersCount + 1
                            )
                        )
                    }
                } else {
                    _followAction.value = Resource.Error(response.message() ?: "Failed to follow")
                }
            } catch (e: Exception) {
                _followAction.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            _followAction.value = Resource.Loading()
            try {
                val response = repository.unfollowUser(userId)
                if (response.isSuccessful && response.body() != null) {
                    _followAction.value = Resource.Success(response.body()!!)
                    val currentProfile = _userProfile.value
                    if (currentProfile is Resource.Success && currentProfile.data != null) {
                        _userProfile.value = Resource.Success(
                            currentProfile.data.copy(
                                isFollowing = false,
                                followersCount = (currentProfile.data.followersCount - 1).coerceAtLeast(0)
                            )
                        )
                    }
                } else {
                    _followAction.value = Resource.Error(response.message() ?: "Failed to unfollow")
                }
            } catch (e: Exception) {
                _followAction.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            _followers.value = Resource.Loading()
            try {
                val response = repository.getFollowers(userId)
                if (response.isSuccessful && response.body() != null) {
                    _followers.value = Resource.Success(response.body()!!)
                } else {
                    _followers.value = Resource.Error(response.message() ?: "Failed to load followers")
                }
            } catch (e: Exception) {
                _followers.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            _following.value = Resource.Loading()
            try {
                val response = repository.getFollowing(userId)
                if (response.isSuccessful && response.body() != null) {
                    _following.value = Resource.Success(response.body()!!)
                } else {
                    _following.value = Resource.Error(response.message() ?: "Failed to load following")
                }
            } catch (e: Exception) {
                _following.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }

    fun clearFollowAction() {
        _followAction.value = null
    }

    fun clearProfileUpdate() {
        _profileUpdate.value = null
    }
}
