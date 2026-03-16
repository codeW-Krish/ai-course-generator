package com.example.jetpackdemo.data.api

import android.content.Context
import com.example.jetpackdemo.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(
    context: Context
) : Interceptor {

    private val tokenManager = TokenManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val idToken = tokenManager.getIdToken()

        val request = originalRequest.addAuthHeader(idToken)
        val response = chain.proceed(request)

        if (response.code == 401) {
            tokenManager.clearTokens()
        }

        return response
    }

    private fun Request.addAuthHeader(token: String?): Request {
        return if (token.isNullOrEmpty()) this
        else newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
    }
}