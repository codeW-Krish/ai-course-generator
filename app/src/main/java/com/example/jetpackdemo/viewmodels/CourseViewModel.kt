package com.example.jetpackdemo.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// A sealed class to wrap state (you can reuse this for all API calls)
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {
//
//    private val _outline = MutableLiveData<Resource<GenerateOutlineResponse>>()
//    val outline: LiveData<Resource<GenerateOutlineResponse>> = _outline

    private val _content = MutableLiveData<Resource<ContentGenerationStatusResponse>>()
    val content: LiveData<Resource<ContentGenerationStatusResponse>> = _content

    private val _status = MutableLiveData<Resource<GenerationStatusResponse>>()
    val status: LiveData<Resource<GenerationStatusResponse>> = _status

    private val _publicCourses = MutableLiveData<Resource<CoursesResponse>>()
    val publicCourses: LiveData<Resource<CoursesResponse>> = _publicCourses

    private val _myCourses = MutableLiveData<Resource<CoursesResponse>>()
    val myCourses: LiveData<Resource<CoursesResponse>> = _myCourses

    private val _enrolledCourses = MutableLiveData<Resource<CoursesResponse>>()
    val enrolledCourses: LiveData<Resource<CoursesResponse>> = _enrolledCourses

    private val _enroll = MutableLiveData<Resource<EnrollResponse>>()
    val enroll: LiveData<Resource<EnrollResponse>> = _enroll

    private val _fullCourse = MutableLiveData<Resource<CourseFullResponse>>()
    val fullCourse: LiveData<Resource<CourseFullResponse>> = _fullCourse

    private val _outlineState = MutableStateFlow<CourseOutline?>(null)
    val outlineState: StateFlow<CourseOutline?> = _outlineState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _updateOutline = MutableStateFlow<Resource<GenerateOutlineResponse>?>(null)
    val updateOutline = _updateOutline.asStateFlow()


    private val _courseId = MutableStateFlow<String?>(null)
    val courseId = _courseId.asStateFlow()



    private var request: GenerateOutlineRequest? = null
    fun prepareOutlineRequest(
        title: String,
        description: String,
        numUnits: Int,
        difficulty: String,
        includeVideos: Boolean
    ) {
        request = GenerateOutlineRequest(
            title = title,
            description = description,
            numUnits = numUnits,
            difficulty = difficulty,
            includeVideos = includeVideos
        )
    }
    // ---- API CALLS ----

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun generateCourseOutline() {
        val req = request ?: return
        viewModelScope.launch {
            _isLoading.value = true
//            _outline.value = Resource.Loading()
            try {
                val response = repository.generateCourseOutline(req)
                Log.d("CourseViewModel", "Response code: ${response.code()}")
                if (response.isSuccessful) {
//                    _outline.value = Resource.Success(response.body()!!)
                    val body = response.body()
                    Log.d("CourseViewModel", "Response body: $body")
                    _courseId.value = response.body()?.courseId
                    _outlineState.value = response.body()?.outline
                } else {
//                    _outlineState.value = Resource.Error("Failed: ${response.code()}")
                    Log.e("CourseViewModel", "Error response: ${response.errorBody()?.string()}")
                    _outlineState.value = null
                }
            } catch (e: Exception) {
//                _outlineState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
                Log.e("CourseViewModel", "Exception in generateCourseOutline", e)
                _outlineState.value = null
            }finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCourseOutlineBeforeGeneration(courseId: String, outline: CourseOutline, regenerate:Boolean = false){
        viewModelScope.launch {
            _updateOutline.value = Resource.Loading();
            try {
                val response = repository.updateOutlineBeforeGenerationConetent(courseId, outline, regenerate);
                if (response.isSuccessful) {
                    _updateOutline.value = Resource.Success(response.body()!!)
                    _outlineState.value = response.body()?.outline
                } else {
                    _updateOutline.value = Resource.Error("Failed to update outline: ${response.code()}")
                }
            }catch (e: Exception){
                _updateOutline.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun generateContent(courseId: String) {
        viewModelScope.launch {
            _content.value = Resource.Loading()
            try {
                val response = repository.generateContent(courseId)
                if (response.isSuccessful) {
                    _content.value = Resource.Success(response.body()!!)
                } else {
                    _content.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _content.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun getCourseGenerationStatus(courseId: String, since: String? = null) {
        viewModelScope.launch {
            _status.value = Resource.Loading()
            try {
                val response = repository.getCourseGenerationStatus(courseId, since)
                if (response.isSuccessful) {
                    _status.value = Resource.Success(response.body()!!)
                } else {
                    _status.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _status.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun getAllPublicCourses() {
        viewModelScope.launch {
            _publicCourses.value = Resource.Loading()
            try {
                val response = repository.getAllPublicCourses()
                if (response.isSuccessful) {
                    _publicCourses.value = Resource.Success(response.body()!!)
                } else {
                    _publicCourses.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _publicCourses.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun getMyCourses() {
        viewModelScope.launch {
            _myCourses.value = Resource.Loading()
            try {
                val response = repository.getMyCourses()
                if (response.isSuccessful) {
                    _myCourses.value = Resource.Success(response.body()!!)
                } else {
                    _myCourses.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _myCourses.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun getEnrolledCourses() {
        viewModelScope.launch {
            _enrolledCourses.value = Resource.Loading()
            try {
                val response = repository.getEnrolledCourses()
                if (response.isSuccessful) {
                    _enrolledCourses.value = Resource.Success(response.body()!!)
                } else {
                    _enrolledCourses.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _enrolledCourses.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _enroll.value = Resource.Loading()
            try {
                val response = repository.enrollInCourse(courseId)
                if (response.isSuccessful) {
                    _enroll.value = Resource.Success(response.body()!!)
                } else {
                    _enroll.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _enroll.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun getFullCourse(courseId: String) {
        viewModelScope.launch {
            _fullCourse.value = Resource.Loading()
            try {
                val response = repository.getFullCourse(courseId)
                if (response.isSuccessful) {
                    _fullCourse.value = Resource.Success(response.body()!!)
                } else {
                    _fullCourse.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _fullCourse.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

}
