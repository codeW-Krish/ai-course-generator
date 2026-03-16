package com.example.jetpackdemo.data.api

import okhttp3.Interceptor
import okhttp3.Response


class ChunkedEncodingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse.newBuilder()
            .header("Transfer-Encoding", "chunked")
            .body(originalResponse.body) // This prevents buffering
            .build()
    }
}
