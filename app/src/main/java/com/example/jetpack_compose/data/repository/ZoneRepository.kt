package com.example.jetpack_compose.data.repository

import com.example.jetpack_compose.data.model.Zona
import com.example.jetpack_compose.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ZoneRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getZonas(): Flow<List<Zona>> = callbackFlow {
        val listener = db.collection(Constants.Collections.ZONAS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val zonas = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Zona::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(zonas)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserLocation(userId: String, lat: Double, lng: Double) {
        db.collection(Constants.Collections.UBICACIONES).document(userId).set(
            mapOf(
                "usuarioId" to userId,
                "ubicacion" to GeoPoint(lat, lng),
                "ultimaActualizacion" to com.google.firebase.Timestamp.now()
            )
        ).await()
    }

    suspend fun sendAlerta(userId: String, mensaje: String, tipo: String) {
        db.collection(Constants.Collections.ALERTAS).add(
            mapOf(
                "fecha" to com.google.firebase.Timestamp.now(),
                "mensaje" to mensaje,
                "tipo" to tipo,
                "usuarioId" to userId
            )
        ).await()
    }
}
