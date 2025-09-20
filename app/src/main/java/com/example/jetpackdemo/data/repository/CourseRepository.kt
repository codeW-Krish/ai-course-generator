package com.example.jetpackdemo.data.repository

import com.example.jetpackdemo.data.api.ApiService
import com.example.jetpackdemo.data.model.*
import retrofit2.Response

class CourseRepository(private val api: ApiService) {

    suspend fun generateCourseOutline(request: GenerateOutlineRequest): Response<GenerateOutlineResponse> {
        return api.generateOutline(request)
    }

    suspend fun generateContent(courseId: String): Response<ContentGenerationStatusResponse> {
        return api.generateContent(courseId)
    }

    suspend fun getCourseGenerationStatus(courseId: String, since: String? = null): Response<GenerationStatusResponse> {
        return api.getCourseGenerationStatus(courseId, since)
    }

    suspend fun getAllPublicCourses(): Response<CoursesResponse> {
        return api.getAllPublicCourses()
    }

    suspend fun getMyCourses(): Response<CoursesResponse> {
        return api.getMyCourses()
    }

    suspend fun getEnrolledCourses(): Response<CoursesResponse> {
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
}
