package com.example.jetpack_compose.data.repository

import com.example.jetpack_compose.data.model.SafeRoute
import com.example.jetpack_compose.data.model.Valoracion
import com.example.jetpack_compose.util.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RouteRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getAllRoutes(): Flow<List<SafeRoute>> = callbackFlow {
        val listener = db.collection(Constants.Collections.RUTAS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val routes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SafeRoute::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.recomendacion } ?: emptyList()
                trySend(routes)
            }
        awaitClose { listener.remove() }
    }

    fun getRoutesByCategory(categoria: String): Flow<List<SafeRoute>> = callbackFlow {
        val listener = db.collection(Constants.Collections.RUTAS)
            .whereEqualTo("categoria", categoria)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val routes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SafeRoute::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(routes)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getRouteById(routeId: String): SafeRoute? {
        val doc = db.collection(Constants.Collections.RUTAS).document(routeId).get().await()
        return doc.toObject(SafeRoute::class.java)?.copy(id = doc.id)
    }

    suspend fun saveRoute(route: SafeRoute): Result<String> = runCatching {
        val data = route.copy(fechaCreacion = route.fechaCreacion ?: Timestamp.now())
        if (route.id.isNotEmpty()) {
            db.collection(Constants.Collections.RUTAS).document(route.id).set(data).await()
            route.id
        } else {
            val ref = db.collection(Constants.Collections.RUTAS).add(data).await()
            ref.id
        }
    }

    fun getValoraciones(routeId: String): Flow<List<Valoracion>> = callbackFlow {
        val listener = db.collection(Constants.Collections.RUTAS).document(routeId)
            .collection(Constants.Collections.VALORACIONES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Valoracion::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.fecha?.seconds ?: 0 } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addValoracion(routeId: String, valoracion: Valoracion): Result<Unit> = runCatching {
        val valRef = db.collection(Constants.Collections.RUTAS).document(routeId)
            .collection(Constants.Collections.VALORACIONES)
            .add(valoracion.copy(fecha = Timestamp.now()))
            .await()

        val routeDoc = db.collection(Constants.Collections.RUTAS).document(routeId).get().await()
        val route = routeDoc.toObject(SafeRoute::class.java) ?: return@runCatching
        val valoraciones = db.collection(Constants.Collections.RUTAS).document(routeId)
            .collection(Constants.Collections.VALORACIONES).get().await()

        val total = valoraciones.size()
        val promedio = if (total > 0) {
            valoraciones.documents.sumOf { it.toObject(Valoracion::class.java)?.puntuacion ?: 0 } / total.toDouble()
        } else 0.0

        db.collection(Constants.Collections.RUTAS).document(routeId).update(
            mapOf(
                "recomendacion" to promedio,
                "totalValoraciones" to total
            )
        ).await()
        valRef.id
    }

    suspend fun toggleFavorite(userId: String, routeId: String, route: SafeRoute): Result<Boolean> =
        runCatching {
            val favRef = db.collection(Constants.Collections.USERS).document(userId)
                .collection(Constants.Collections.FAVORITOS).document(routeId)
            val exists = favRef.get().await().exists()
            if (exists) {
                favRef.delete().await()
                false
            } else {
                favRef.set(route.copy(id = routeId)).await()
                true
            }
        }

    suspend fun isFavorite(userId: String, routeId: String): Boolean {
        val doc = db.collection(Constants.Collections.USERS).document(userId)
            .collection(Constants.Collections.FAVORITOS).document(routeId).get().await()
        return doc.exists()
    }

    fun getFavorites(userId: String): Flow<List<SafeRoute>> = callbackFlow {
        val listener = db.collection(Constants.Collections.USERS).document(userId)
            .collection(Constants.Collections.FAVORITOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val routes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SafeRoute::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(routes)
            }
        awaitClose { listener.remove() }
    }
}
