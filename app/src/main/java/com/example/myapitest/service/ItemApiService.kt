package com.example.myapitest.service

import com.example.myapitest.model.Item
import com.example.myapitest.model.ItemValue
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ItemApiService {
    @GET("car")
    suspend fun getItems(): List<ItemValue>

    @GET("car/{id}")
    suspend fun getItem(@Path("id") id: String): Item

    @POST("car")
    suspend fun addItem(@Body item: ItemValue): Item

    @PATCH("car/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: ItemValue): Item

    @DELETE("car/{id}")
    suspend fun deleteItem(@Path("id") id: String)

}