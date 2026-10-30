package com.example.aprendeapp.network

import com.example.aprendeapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Reemplaza esta dirección por la URL de tu colección MockAPI, terminada en /.
    private const val BASE_URL = "https://TU_ID.mockapi.io/api/v1/"
    val service: ApiService by lazy {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        Retrofit.Builder().baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().addInterceptor(logger).build())
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
    }
}
