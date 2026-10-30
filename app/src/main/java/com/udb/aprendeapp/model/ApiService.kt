package com.udb.aprendeapp.model

import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("recursos")
    fun getResources(): Call<List<Resource>>

    @GET("recursos/{id}")
    fun getResourceById(@Path("id") id: String): Call<Resource>

    @POST("recursos")
    fun createResource(@Body resource: Resource): Call<Resource>

    @PUT("recursos/{id}")
    fun updateResource(@Path("id") id: String, @Body resource: Resource): Call<Resource>

    @DELETE("recursos/{id}")
    fun deleteResource(@Path("id") id: String): Call<Void>
}
