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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    var nombreZona by remember { mutableStateOf("") }
    var nivelRiesgo by remember { mutableStateOf("Bajo") }
    var radio by remember { mutableStateOf("50") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Obtener ubicación
            }
        }
    )

    fun obtenerUbicacionActual() {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    latitud = it.latitude.toString()
                    longitud = it.longitude.toString()
                }
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Zona Peligrosa") },
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
                "Información de la Zona",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombreZona,
                onValueChange = { nombreZona = it },
                label = { Text("Nombre de la zona / Incidente") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Nivel de Riesgo", modifier = Modifier.fillMaxWidth())
            val riesgos = listOf("Bajo", "Medio", "Alto", "Crítico")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                riesgos.forEach { riesgo ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (nivelRiesgo == riesgo),
                            onClick = { nivelRiesgo = riesgo }
                        )
                        Text(riesgo, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = radio,
                onValueChange = { radio = it },
                label = { Text("Radio de influencia (metros)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = latitud,
                        onValueChange = { latitud = it },
                        label = { Text("Latitud") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = longitud,
                        onValueChange = { longitud = it },
                        label = { Text("Longitud") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                IconButton(
                    onClick = { obtenerUbicacionActual() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Mi ubicación", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isSaving) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (nombreZona.isNotEmpty() && latitud.isNotEmpty() && longitud.isNotEmpty()) {
                            isSaving = true
                            val zona = hashMapOf(
                                "nombre" to nombreZona,
                                "nivelRiesgo" to nivelRiesgo,
                                "radio" to radio,
                                "latitud" to latitud,
                                "longitud" to longitud,
                                "ultimaActualizacion" to Timestamp.now()
                            )
                            
                            db.collection("zonas_peligrosas")
                                .add(zona)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Zona reportada con éxito", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Zona Peligrosa")
                }
            }
        }
    }
}
