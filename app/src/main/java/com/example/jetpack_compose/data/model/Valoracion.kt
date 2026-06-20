package com.example.jetpack_compose.data.model

import com.google.firebase.Timestamp

data class Valoracion(
    val id: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val puntuacion: Int = 0,
    val comentario: String = "",
    val fecha: Timestamp? = null
)
