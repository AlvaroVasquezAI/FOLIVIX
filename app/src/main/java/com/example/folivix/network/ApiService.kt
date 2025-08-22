package com.example.folivix.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("predict")
    suspend fun predictDisease(
        @Part image: MultipartBody.Part
    ): Response<PredictionResponse>
}