package com.example.jetpack_compose.data.repository

import com.example.jetpack_compose.data.local.PendingReportLocalStore
import com.example.jetpack_compose.data.local.entity.PendingReporteEntity
import kotlinx.coroutines.flow.Flow

class SecurityRepository(private val localStore: PendingReportLocalStore) {
    val pendingReportes: Flow<List<PendingReporteEntity>> = localStore.pending

    suspend fun addPendingReporte(reporte: PendingReporteEntity) {
        localStore.insert(reporte)
    }
}
