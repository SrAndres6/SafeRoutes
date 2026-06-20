package com.example.jetpack_compose.data.model

data class Zona(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val nivelRiesgo: String = "Bajo",
    val radio: String = "50"
)
