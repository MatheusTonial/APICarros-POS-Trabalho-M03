package com.tonial.apicarros.service

import com.tonial.apicarros.model.Carro
import retrofit2.http.GET

interface ItemApiService {

    @GET("car")
    suspend fun getCars(): List<Carro>

}