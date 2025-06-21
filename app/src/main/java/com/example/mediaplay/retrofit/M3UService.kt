package com.example.mediaplay.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface M3UService {
    @GET
    suspend fun getM3UPlaylist(@Url url: String): Response<ResponseBody>
}
