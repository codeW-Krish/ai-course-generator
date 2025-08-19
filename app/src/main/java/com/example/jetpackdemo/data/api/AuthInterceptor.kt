package com.example.jetpackdemo.data.api

import android.content.Context
import com.example.jetpackdemo.data.model.RefreshRequest
import com.example.jetpackdemo.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val context: Context,
    private val refreshApi: ApiService
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val tokenManager = TokenManager(context)
        val accessToken = tokenManager.getAccessToken()

        // Attach access token to the original request if available
        if (!accessToken.isNullOrEmpty()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        val response = chain.proceed(request)

        // If the access token is expired
        if (response.code == 401) {
            val refreshToken = tokenManager.getRefreshToken()

            if (!refreshToken.isNullOrEmpty()) {
                val refreshResponse = runBlocking {
                    try {
                        refreshApi.refresh(RefreshRequest(refreshToken))
                    } catch (e: Exception) {
                        null
                    }
                }

                if (refreshResponse?.isSuccessful == true && refreshResponse.body() != null) {
                    val newTokens = refreshResponse.body()!!

                    // Save the new tokens
                    tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                    // Retry the request with new access token
                    val newRequest = request.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()

                    return chain.proceed(newRequest)
                } else {
                    // Refresh token is invalid or failed
                    tokenManager.clearTokens()
                    throw IOException("Session expired. Please log in again.")
                }
            }
        }

        return response
    }
}
