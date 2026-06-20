package com.example.jetpack_compose.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpack_compose.data.model.SafeRoute
import com.example.jetpack_compose.ui.homeViewModel

@Composable
fun HomeScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToRouteDetail: (String) -> Unit
) {
    val viewModel = homeViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SafeRoute",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Bienvenido, ${uiState.userName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Rutas seguras para peatones, ciclistas y senderistas",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        LargeActionButton(
            title = "Iniciar Viaje",
            subtitle = "Calcula y navega tu ruta",
            icon = Icons.Default.DirectionsRun,
            color = MaterialTheme.colorScheme.primaryContainer,
            onClick = onNavigateToMap
        )

        Spacer(modifier = Modifier.height(12.dp))

        LargeActionButton(
            title = "Explorar Rutas",
            subtitle = "Rutas recomendadas por la comunidad",
            icon = Icons.Default.Route,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = onNavigateToRoutes
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.topRoutes.isNotEmpty()) {
            Text(
                "Rutas mejor valoradas",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            uiState.topRoutes.forEach { route ->
                RouteCard(route = route, onClick = { onNavigateToRouteDetail(route.id) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            SmallActionButton(
                title = "Reportar",
                icon = Icons.Default.AddAlert,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToReport
            )
            Spacer(modifier = Modifier.width(16.dp))
            SmallActionButton(
                title = "Mi Perfil",
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToProfile
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToEmergency,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("BOTÓN SOS / ACOMPÁÑAME", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun RouteCard(route: SafeRoute, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(route.nombre, fontWeight = FontWeight.Bold)
                Text(
                    "${route.categoria} • ${route.dificultad} • ${route.distanciaTexto}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                Text(
                    String.format("%.1f", route.recomendacion),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun LargeActionButton(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SmallActionButton(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Medium)
        }
    }
}
