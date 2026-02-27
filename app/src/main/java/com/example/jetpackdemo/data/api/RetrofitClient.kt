package com.example.jetpackdemo.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://e31a-2409-40c1-4110-42-c981-eb22-b803-3180.ngrok-free.app"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val baseOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(50, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val publicApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(baseOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }


    fun getAuthApi(context: Context): ApiService {
        val authClient = baseOkHttpClient.newBuilder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getOkHttpClientForSSE(context: Context): OkHttpClient {
        return baseOkHttpClient.newBuilder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(ChunkedEncodingInterceptor())
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Auth API with extended timeout for long-running operations
     * (e.g. video manifest generation — 3-5 min of LLM + TTS + image calls)
     */
    fun getLongRunningAuthApi(context: Context): ApiService {
        val longClient = baseOkHttpClient.newBuilder()
            .addInterceptor(AuthInterceptor(context))
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(longClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}