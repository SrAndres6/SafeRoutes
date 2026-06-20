package com.example.jetpack_compose.data.model

import com.google.firebase.Timestamp

data class Reporte(
    val id: String = "",
    val rutaId: String = "",
    val rutaNombre: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val fecha: Timestamp? = null
)
