package com.example.jetpack_compose.data.model

import com.google.firebase.Timestamp

data class SafeRoute(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "URBANA",
    val distanciaMetros: Int = 0,
    val distanciaTexto: String = "",
    val tiempoMinutos: Int = 0,
    val tiempoTexto: String = "",
    val dificultad: String = "FACIL",
    val recomendacion: Double = 0.0,
    val totalValoraciones: Int = 0,
    val origenLat: Double = 0.0,
    val origenLng: Double = 0.0,
    val origenNombre: String = "",
    val destinoLat: Double = 0.0,
    val destinoLng: Double = 0.0,
    val destinoNombre: String = "",
    val polyline: String = "",
    val modoTransporte: String = "walking",
    val creadorId: String = "",
    val fechaCreacion: Timestamp? = null
)
