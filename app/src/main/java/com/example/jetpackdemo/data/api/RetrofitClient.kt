package com.example.jetpackdemo.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://52cabce422fb.ngrok-free.app/" // Use this for Android Emulator

    // Shared logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Shared base OkHttpClient (timeouts + logging)
    private val baseOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(50, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    // --- Public API Client (for /login, /register) ---
    val publicApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // --- Authenticated API Client (with AuthInterceptor) ---
    fun getAuthApi(context: Context): ApiService {
        val refreshApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        val authClient = baseOkHttpClient.newBuilder()
            .addInterceptor(AuthInterceptor(context, refreshApi))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // --- NEW: Expose OkHttpClient for SSE Streaming ---
    fun getOkHttpClientForSSE(context: Context): OkHttpClient {
        val refreshApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        return baseOkHttpClient.newBuilder()
            .addInterceptor(AuthInterceptor(context, refreshApi))
            .addInterceptor(ChunkedEncodingInterceptor())
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
    }
}