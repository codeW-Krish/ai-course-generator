package com.example.jetpackdemo.data.repository

import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.api.GenericResponse
import com.example.jetpackdemo.data.model.*
import retrofit2.Response

class CourseRepository(private val api: ApiService) {

    suspend fun generateCourseOutline(request: GenerateOutlineRequest): Response<GenerateOutlineResponse> {
        return api.generateOutline(request)
    }

    // Add this method to your CourseRepository class
    suspend fun generateContent(courseId: String, provider: String? = null, model: String? = null): Response<ContentGenerationStatusResponse> {
        return api.generateContent(courseId, provider, model)
    }

    suspend fun getCourseGenerationStatus(courseId: String, since: String? = null): Response<GenerationStatusResponse> {
        return api.getCourseGenerationStatus(courseId, since)
    }

    suspend fun getAllPublicCourses(): Response<CoursesResponse> {
        return api.getAllPublicCourses()
    }

    suspend fun getMyCourses(): Response<MyCoursesResponse> {
        return api.getMyCourses()
    }

    suspend fun getEnrolledCourses(): Response<enrolledCoursesResponse> {
        return api.getEnrolledCourses()
    }

    suspend fun enrollInCourse(courseId: String): Response<EnrollResponse> {
        return api.enrollInCourse(courseId)
    }

    suspend fun getFullCourse(courseId: String): Response<CourseFullResponse> {
        return api.getFullCourse(courseId)
    }

    suspend fun updateOutlineBeforeGenerationConetent(courseId: String, outline: CourseOutline, regenerate: Boolean = false): Response<GenerateOutlineResponse>{
        return api.updateCourseOutline(courseId, outline, regenerate);
    }

    // Admin methods
    suspend fun getAllCoursesAdmin(): Response<CoursesResponse> {
        return api.getAllCoursesAdmin()
    }

    suspend fun deleteCourseAdmin(courseId: String): Response<GenericResponse> {
        return api.deleteCourseAdmin(courseId)
    }

    suspend fun getGlobalSettings(): Response<GlobalSettingsResponse> {
        return api.getGlobalSettings()
    }

    suspend fun updateGlobalSetting(key: String, value: String): Response<GenericResponse> {
        return api.updateGlobalSetting(key, mapOf("value" to value))
    }

    suspend fun updateAvailableProviders(providers: List<String>): Response<GenericResponse> {
        return api.updateAvailableProviders(UpdateProvidersRequest(providers))
    }

    suspend fun updateDefaultProviders(outlineProvider: String, contentProvider: String): Response<GenericResponse> {
        return api.updateDefaultProviders(UpdateDefaultProvidersRequest(outlineProvider, contentProvider))
    }

    // Public settings methods
    suspend fun getAvailableProviders(): Response<AvailableProvidersResponse> {
        return api.getAvailableProviders()
    }

    suspend fun getDefaultProviders(): Response<DefaultProvidersResponse> {
        return api.getDefaultProviders()
    }

    suspend fun searchCourses(query: String) = api.searchCourses(query)
    suspend fun searchCoursesFull(q: String, d: String?, s: String?) = api.searchFull(q, d, s)
    suspend fun deleteMyCourse(id: String) = api.deleteCourse(id)
    suspend fun saveNote(id: String, note: String) = api.saveNote(id, mapOf("note" to note))
    suspend fun getNote(id: String) = api.getNote(id)
    suspend fun markComplete(id: String, completed: Boolean) = api.markComplete(id, mapOf("completed" to completed))
    suspend fun getCourseProgress(courseId: String) = api.getProgress(courseId)

    // Unenroll from course
    suspend fun unenrollFromCourse(courseId: String) = api.unenrollFromCourse(courseId)

    // Logout (server-side session invalidation)
    suspend fun logout() = api.logout()
}
