package com.example.jetpackdemo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.data.api.GenericResponse
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.CourseRepository
import com.example.jetpackdemo.shared_pref.UserPreferencesManager
import com.example.jetpackdemo.viewmodels.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class AdminViewModel(
    application: Application,
    private val repository: CourseRepository
) : AndroidViewModel(application) {

    private val userPrefsManager = UserPreferencesManager(application)

    private val _globalSettings = MutableStateFlow<Resource<GlobalSettingsResponse>?>(null)
    val globalSettings: StateFlow<Resource<GlobalSettingsResponse>?> =
        _globalSettings.asStateFlow()

    private val _availableProviders =
        MutableStateFlow<Resource<AvailableProvidersResponse>?>(null)
    val availableProviders: StateFlow<Resource<AvailableProvidersResponse>?> =
        _availableProviders.asStateFlow()

    private val _defaultProviders = MutableStateFlow<Resource<DefaultProvidersResponse>?>(null)
    val defaultProviders: StateFlow<Resource<DefaultProvidersResponse>?> =
        _defaultProviders.asStateFlow()

    private val _adminCourses = MutableStateFlow<Resource<CoursesResponse>?>(null)
    val adminCourses: StateFlow<Resource<CoursesResponse>?> = _adminCourses.asStateFlow()

    private val _updateStatus = MutableStateFlow<Resource<GenericResponse>?>(null)
    val updateStatus: StateFlow<Resource<GenericResponse>?> = _updateStatus.asStateFlow()

    private val _users = MutableStateFlow<Map<String, String>>(emptyMap())
    val users = _users.asStateFlow()

//    fun loadUsers() { ... }

    val isAdmin: Boolean
        get() = userPrefsManager.isAdmin()

    // Load global settings
    fun loadGlobalSettings() {
        if (!isAdmin) return

        viewModelScope.launch {
            _globalSettings.value = Resource.Loading()
            try {
                val response = repository.getGlobalSettings()
                if (response.isSuccessful) {
                    _globalSettings.value = Resource.Success(response.body()!!)
                } else {
                    _globalSettings.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _globalSettings.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Load available providers
    fun loadAvailableProviders() {
        viewModelScope.launch {
            _availableProviders.value = Resource.Loading()
            try {
                val response = repository.getAvailableProviders()
                if (response.isSuccessful) {
                    _availableProviders.value = Resource.Success(response.body()!!)
                } else {
                    _availableProviders.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _availableProviders.value =
                    Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Load default providers
    fun loadDefaultProviders() {
        viewModelScope.launch {
            _defaultProviders.value = Resource.Loading()
            try {
                val response = repository.getDefaultProviders()
                if (response.isSuccessful) {
                    _defaultProviders.value = Resource.Success(response.body()!!)
                } else {
                    _defaultProviders.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _defaultProviders.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Update available providers
    fun updateAvailableProviders(providers: List<String>) {
        if (!isAdmin) return

        viewModelScope.launch {
            _updateStatus.value = Resource.Loading()
            try {
                val response = repository.updateAvailableProviders(providers)
                if (response.isSuccessful) {
                    _updateStatus.value = Resource.Success(response.body()!!)
                    // Reload available providers
                    loadAvailableProviders()
                } else {
                    _updateStatus.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _updateStatus.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Update default providers
    fun updateDefaultProviders(outlineProvider: String, contentProvider: String) {
        if (!isAdmin) return

        viewModelScope.launch {
            _updateStatus.value = Resource.Loading()
            try {
                val response =
                    repository.updateDefaultProviders(outlineProvider, contentProvider)
                if (response.isSuccessful) {
                    _updateStatus.value = Resource.Success(response.body()!!)
                    // Reload default providers
                    loadDefaultProviders()
                } else {
                    _updateStatus.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _updateStatus.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Load all courses for admin
    fun loadAllCoursesAdmin() {
        if (!isAdmin) return

        viewModelScope.launch {
            _adminCourses.value = Resource.Loading()
            try {
                val response = repository.getAllCoursesAdmin()
                if (response.isSuccessful) {
                    _adminCourses.value = Resource.Success(response.body()!!)
                } else {
                    _adminCourses.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _adminCourses.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // === DELETE COURSE ===
    fun deleteCourse(courseId: String) {
        if (!isAdmin) return

        viewModelScope.launch {
            _updateStatus.value = Resource.Loading()
            try {
                val response = repository.deleteCourseAdmin(courseId)
                if (response.isSuccessful) {
                    _updateStatus.value = Resource.Success(response.body()!!)
                    // Reload courses to reflect deletion
                    loadAllCoursesAdmin()
                } else {
                    _updateStatus.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _updateStatus.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Clear update status
    fun clearUpdateStatus() {
        _updateStatus.value = null
    }
}
