package com.tonial.apicarros.service

import com.tonial.apicarros.database.DatabaseBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.3.100:3000/" // Endereço usado para acessar o localhost no emulador Android

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(GeoLocationInterceptor(DatabaseBuilder.getInstance().userLocationDao()))
        .build()


    val apiService: CarroApiService by lazy {
        retrofit.create(CarroApiService::class.java)
    }

}