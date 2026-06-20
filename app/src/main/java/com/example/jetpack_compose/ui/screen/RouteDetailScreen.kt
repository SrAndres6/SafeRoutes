package com.example.jetpack_compose.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpack_compose.ui.routeDetailViewModel
import com.example.jetpack_compose.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    routeId: String,
    onBack: () -> Unit
) {
    val viewModel = routeDetailViewModel(routeId)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var puntuacion by remember { mutableIntStateOf(5) }
    var comentario by remember { mutableStateOf("") }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.route?.nombre ?: "Detalle de ruta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (uiState.isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val route = uiState.route
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                route?.let { r ->
                    item {
                        Text(r.descripcion, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Categoría: ${r.categoria} • Dificultad: ${r.dificultad}")
                        Text("Distancia: ${r.distanciaTexto} • Tiempo: ${r.tiempoTexto}")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                            Text(
                                " ${String.format("%.1f", r.recomendacion)} (${r.totalValoraciones} valoraciones)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        HorizontalDivider()
                        Text("Calificar ruta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { puntuacion = star }) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (star <= puntuacion) Color(0xFFFFC107) else Color.LightGray
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = comentario,
                            onValueChange = { comentario = it },
                            label = { Text("Comentario") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.submitRating(puntuacion, comentario) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar valoración")
                        }
                    }

                    item {
                        HorizontalDivider()
                        Text("Comentarios de la comunidad", fontWeight = FontWeight.Bold)
                    }
                    if (uiState.valoraciones.isEmpty()) {
                        item { Text("Sin valoraciones aún", color = Color.Gray) }
                    } else {
                        items(uiState.valoraciones) { val_ ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(val_.usuarioNombre, fontWeight = FontWeight.Bold)
                                    Text("★ ${val_.puntuacion}/5")
                                    Text(val_.comentario)
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                        Text("Reportes comunitarios", fontWeight = FontWeight.Bold)
                    }
                    if (uiState.reportes.isEmpty()) {
                        item { Text("Sin reportes para esta ruta", color = Color.Gray) }
                    } else {
                        items(uiState.reportes) { reporte ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(reporteTipoLabel(reporte.tipo), fontWeight = FontWeight.Bold)
                                    Text(reporte.descripcion)
                                    Text("Por: ${reporte.usuarioNombre}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun reporteTipoLabel(tipo: String): String = when (tipo) {
    Constants.ReporteTipo.CAMINO_DANADO -> "Camino dañado"
    Constants.ReporteTipo.SENDERO_BLOQUEADO -> "Sendero bloqueado"
    Constants.ReporteTipo.CAMBIO_RUTA -> "Cambio en el recorrido"
    Constants.ReporteTipo.CONDICION_ACTUAL -> "Condición actual del camino"
    else -> tipo
}
