package com.example.myapitest.service

import com.example.myapitest.service.ItemApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Address used to access localhost on the Android emulator
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val itemApiService: ItemApiService by lazy {
        retrofit.create(ItemApiService::class.java)
    }
}