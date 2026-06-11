package com.example.jetpack_compose.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpack_compose.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // Estado para almacenar los datos del usuario
    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar datos al iniciar la pantalla
    LaunchedEffect(Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userData = document.toObject(User::class.java)
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configuración de Seguridad") })
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // Info de Usuario
                    Surface(
                        modifier = Modifier.size(100.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Iniciales dinámicas
                            val initials = userData?.name?.take(2)?.uppercase() ?: "U"
                            Text(initials, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre dinámico
                    Text(
                        text = userData?.name ?: "Usuario",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = userData?.email ?: "Sin correo",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ... resto de los items (Contactos, etc)
            }
        }
    }
}