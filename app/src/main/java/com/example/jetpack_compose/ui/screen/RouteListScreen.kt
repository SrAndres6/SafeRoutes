package com.example.jetpack_compose.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpack_compose.ui.routeListViewModel
import com.example.jetpack_compose.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    onBack: () -> Unit,
    onRouteClick: (String) -> Unit
) {
    val viewModel = routeListViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explorar Rutas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Filtrar por categoría", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.filterByCategory(null) },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = uiState.selectedCategory == Constants.Categorias.URBANA,
                    onClick = { viewModel.filterByCategory(Constants.Categorias.URBANA) },
                    label = { Text("Urbana") }
                )
                FilterChip(
                    selected = uiState.selectedCategory == Constants.Categorias.CICLISMO,
                    onClick = { viewModel.filterByCategory(Constants.Categorias.CICLISMO) },
                    label = { Text("Ciclismo") }
                )
                FilterChip(
                    selected = uiState.selectedCategory == Constants.Categorias.SENDERISMO,
                    onClick = { viewModel.filterByCategory(Constants.Categorias.SENDERISMO) },
                    label = { Text("Senderismo") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            } else if (uiState.routes.isEmpty()) {
                Text("No hay rutas disponibles. Crea una desde el mapa.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.routes) { route ->
                        RouteCard(route = route, onClick = { onRouteClick(route.id) })
                    }
                }
            }
        }
    }
}
