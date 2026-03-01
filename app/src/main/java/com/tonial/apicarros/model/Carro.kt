package com.tonial.apicarros.model

// As classes foram reestruturadas para corresponder à resposta "plana" da API.
// A classe CarroValue foi removida e seus campos foram incorporados diretamente na classe Carro.
data class Carro(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Location
)

data class Location(
    val lat: Double,
    val long: Double
)
