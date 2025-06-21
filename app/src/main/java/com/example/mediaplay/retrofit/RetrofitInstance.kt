package com.example.mediaplay.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.example.mediaplay.retrofit.M3UService

object RetrofitInstance {

    private const val BASE_URL = "http://cdn45.cc/"

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val api: M3UService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .build()
            .create(M3UService::class.java)
    }
}
