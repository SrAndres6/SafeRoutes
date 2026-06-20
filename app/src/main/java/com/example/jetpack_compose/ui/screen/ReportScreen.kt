package com.example.jetpack_compose.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.jetpack_compose.ui.reportViewModel
import com.example.jetpack_compose.util.Constants
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = reportViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let { viewModel.setLocation(it.latitude, it.longitude) }
                }
            }
        }
    )

    fun obtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { viewModel.setLocation(it.latitude, it.longitude) }
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    val tiposReporte = listOf(
        Constants.ReporteTipo.CAMINO_DANADO to "Camino dañado",
        Constants.ReporteTipo.SENDERO_BLOQUEADO to "Sendero bloqueado",
        Constants.ReporteTipo.CAMBIO_RUTA to "Cambio en el recorrido",
        Constants.ReporteTipo.CONDICION_ACTUAL to "Condición actual del camino"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte Comunitario") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Comparte información sobre las rutas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedRouteName.ifEmpty { "Seleccionar ruta" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ruta") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.routes.forEach { route ->
                        DropdownMenuItem(
                            text = { Text(route.nombre) },
                            onClick = {
                                viewModel.selectRoute(route.id, route.nombre)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tipo de reporte", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Medium)
            tiposReporte.forEach { (value, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.tipo == value,
                        onClick = { viewModel.setTipo(value) }
                    )
                    Text(label)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = { viewModel.setDescripcion(it) },
                label = { Text("Descripción del reporte") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = uiState.latitud,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Latitud") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.longitud,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Longitud") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                IconButton(onClick = { obtenerUbicacion() }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Mi ubicación", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isSaving) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.submitReport() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Report, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar reporte comunitario")
                }
            }
        }
    }
}
