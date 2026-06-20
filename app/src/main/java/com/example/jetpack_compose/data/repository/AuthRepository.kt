package com.example.jetpack_compose.data.repository

import com.example.jetpack_compose.data.model.User
import com.example.jetpack_compose.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId: String? get() = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(name: String, email: String, password: String, tipoUsuario: String): Result<Unit> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw IllegalStateException("Usuario no creado")
            val user = User(id = userId, name = name, email = email, tipoUsuario = tipoUsuario)
            db.collection(Constants.Collections.USERS).document(userId).set(user).await()
        }

    suspend fun getCurrentUser(): User? {
        val uid = currentUserId ?: return null
        val doc = db.collection(Constants.Collections.USERS).document(uid).get().await()
        return doc.toObject(User::class.java)?.copy(id = uid)
    }

    fun logout() = auth.signOut()
}
