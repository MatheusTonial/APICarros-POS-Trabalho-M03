package com.tonial.apicarros.model

data class Carro(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Location?
)

data class Location(
    val lat: Double,
    val long: Double
)

data class CarroResponse(
    val id: String,
    val value: Carro
)
