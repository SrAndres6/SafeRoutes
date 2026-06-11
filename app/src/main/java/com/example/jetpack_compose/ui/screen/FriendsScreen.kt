package com.example.jetpack_compose.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpack_compose.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val currentUserId = auth.currentUser?.uid ?: ""

    var friends by remember { mutableStateOf<List<User>>(emptyList()) }
    var emailToAdd by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    fun loadFriends() {
        db.collection("users").document(currentUserId).collection("friends")
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { it.toObject(User::class.java) }
                friends = list
                isLoading = false
            }
    }

    LaunchedEffect(Unit) {
        loadFriends()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Amigos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = emailToAdd,
                onValueChange = { emailToAdd = it },
                label = { Text("Agregar por correo") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (emailToAdd.isNotEmpty()) {
                            db.collection("users").whereEqualTo("email", emailToAdd).get()
                                .addOnSuccessListener { result ->
                                    if (!result.isEmpty) {
                                        val friend = result.documents[0].toObject(User::class.java)
                                        if (friend != null) {
                                            db.collection("users").document(currentUserId)
                                                .collection("friends").document(friend.id).set(friend)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Amigo agregado", Toast.LENGTH_SHORT).show()
                                                    emailToAdd = ""
                                                    loadFriends()
                                                }
                                        }
                                    } else {
                                        Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Lista de Amigos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(friends) { friend ->
                        ListItem(
                            headlineContent = { Text(friend.name) },
                            supportingContent = { Text(friend.email) },
                            leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}
