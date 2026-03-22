package com.example.myapitest.service

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgBBService {
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Response<ImgBBResponse>
}

data class ImgBBResponse(
    val data: ImgBBData,
    val success: Boolean,
    val status: Int
)

data class ImgBBData(
    val url: String
)
