package com.udb.aprendeapp.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Configura con tu endpoint de MockAPI.io o Railway
    private const val BASE_URL = "https://651c5e473513a0bc37e937d5.mockapi.io/api/v1/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
