package com.example.myapitest.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val IMGBB_BASE_URL = "https://api.imgbb.com/1/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitImgBB: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(IMGBB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val itemApiService: ItemApiService by lazy {
        retrofit.create(ItemApiService::class.java)
    }

    val imgBBService: ImgBBService by lazy {
        retrofitImgBB.create(ImgBBService::class.java)
    }
}