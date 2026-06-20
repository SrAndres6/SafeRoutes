package com.example.jetpack_compose.data.repository

import com.example.jetpack_compose.data.local.PendingReportLocalStore
import com.example.jetpack_compose.data.local.entity.PendingReporteEntity
import com.example.jetpack_compose.data.model.Reporte
import com.example.jetpack_compose.util.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReportRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val localStore: PendingReportLocalStore
) {
    suspend fun addReporte(reporte: Reporte): Result<String> = runCatching {
        val data = reporte.copy(fecha = reporte.fecha ?: Timestamp.now())
        val ref = db.collection(Constants.Collections.REPORTES).add(data).await()
        ref.id
    }

    suspend fun saveReporteOffline(entity: PendingReporteEntity) {
        localStore.insert(entity)
    }

    suspend fun syncPendingReportes(): Int {
        val pending = localStore.getUnsynced()
        var syncedCount = 0
        for (item in pending) {
            val reporte = Reporte(
                rutaId = item.rutaId,
                rutaNombre = item.rutaNombre,
                tipo = item.tipo,
                descripcion = item.descripcion,
                latitud = item.latitude,
                longitud = item.longitude,
                usuarioId = item.userId,
                usuarioNombre = item.userName
            )
            addReporte(reporte).onSuccess {
                localStore.update(item.copy(synced = true))
                syncedCount++
            }
        }
        return syncedCount
    }

    fun getAllReportes(): Flow<List<Reporte>> = callbackFlow {
        val listener = db.collection(Constants.Collections.REPORTES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Reporte::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.fecha?.seconds ?: 0 } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getReportesByRoute(routeId: String): Flow<List<Reporte>> = callbackFlow {
        val listener = db.collection(Constants.Collections.REPORTES)
            .whereEqualTo("rutaId", routeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Reporte::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}
