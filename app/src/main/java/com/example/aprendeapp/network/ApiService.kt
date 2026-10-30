package com.example.aprendeapp.network

import com.example.aprendeapp.model.Resource
import retrofit2.http.*

interface ApiService {
    @GET("recursos") suspend fun getResources(): List<Resource>
    @POST("recursos") suspend fun createResource(@Body resource: Resource): Resource
    @PUT("recursos/{id}") suspend fun updateResource(@Path("id") id: Int, @Body resource: Resource): Resource
    @DELETE("recursos/{id}") suspend fun deleteResource(@Path("id") id: Int)
    @PATCH("recursos/{id}") suspend fun updateRating(@Path("id") id: Int, @Body rating: Map<String, Int>): Resource
}
