package com.tonial.apicarros.service

import com.tonial.apicarros.model.Carro
import com.tonial.apicarros.model.CarroResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CarroApiService {

    @GET("car")
    suspend fun getCars(): List<Carro>

    @GET("car/{id}")
    suspend fun getCarro(@Path("id") id: String): CarroResponse

    @DELETE("car/{id}")
    suspend fun deleteCarro(@Path("id") id: String)

    @PATCH("car/{id}")
    suspend fun updateCarro(@Path("id") id: String, @Body item: Carro) : Carro

    @POST("car")
    suspend fun addCarro(@Body carro: Carro) : Carro
}