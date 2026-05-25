package com.example.jetpack_compose.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configuración de Seguridad") })
        }
    ) { padding ->
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
                        Text("JA", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Jhon Alexander Lenis", style = MaterialTheme.typography.headlineSmall)
                Text("Usuario SafeRoute Gold", color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SecurityOptionItem(
                    title = "Contactos de Confianza",
                    subtitle = "3 contactos recibirán tu SOS",
                    icon = Icons.Default.Emergency
                )
                SecurityOptionItem(
                    title = "Privacidad de Ubicación",
                    subtitle = "Solo durante viajes activos",
                    icon = Icons.Default.Shield
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Contactos de Emergencia",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Lista de contactos (Placeholder)
            items(2) { index ->
                ListItem(
                    headlineContent = { Text(if(index == 0) "Mamá" else "Hermano") },
                    supportingContent = { Text("+57 300 123 456$index") },
                    trailingContent = { Checkbox(checked = true, onCheckedChange = {}) }
                )
            }

            item {
                TextButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Agregar nuevo contacto")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}

@Composable
fun SecurityOptionItem(title: String, subtitle: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
