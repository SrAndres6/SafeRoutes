package com.example.jetpack_compose.data.local.entity

data class PendingReporteEntity(
    val id: Int = 0,
    val rutaId: String = "",
    val rutaNombre: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val userId: String = "",
    val userName: String = "",
    val synced: Boolean = false,
    val     timestamp: Long = System.currentTimeMillis()
)
