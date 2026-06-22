package com.example.jetpack_compose.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.jetpack_compose.ui.mapViewModel
import com.example.jetpack_compose.ui.viewmodel.TravelMode
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.io.IOException

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    routeId: String? = null,
    onBack: () -> Unit,
    onNavigateToFriends: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = mapViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(6.2442, -75.5812), 12f)
    }

    LaunchedEffect(routeId) {
        routeId?.let {
            viewModel.loadRoute(it)
        }
    }

    fun findLocation(query: String, isOrigin: Boolean) {
        if (query.isEmpty()) return
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                if (isOrigin) {
                    viewModel.setOrigin(addr.latitude, addr.longitude)
                } else {
                    viewModel.setDestination(addr.latitude, addr.longitude)
                }
                scope.launch {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(addr.latitude, addr.longitude), 15f))
                }
            }
        } catch (_: IOException) { }
    }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasLocationPermission = it }
    )

    LaunchedEffect(Unit) {
        findLocation(uiState.originText, true)
        findLocation(uiState.destinationText, false)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Ruta guardada en la comunidad", Toast.LENGTH_SHORT).show()
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.pathPoints) {
        if (uiState.pathPoints.isNotEmpty() && uiState.origin != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder()
                        .target(LatLng(uiState.origin!!.lat, uiState.origin!!.lng))
                        .zoom(17f).tilt(45f).build()
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeRoute") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFriends) {
                        Icon(Icons.Default.Group, contentDescription = "Amigos")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission, isBuildingEnabled = true),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false)
            ) {
                uiState.origin?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.lat, it.lng)),
                        title = "Origen",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
                uiState.destination?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.lat, it.lng)),
                        title = "Destino",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
                uiState.zonas.forEach { zona ->
                    // Círculo de advertencia visual
                    Circle(
                        center = LatLng(zona.latitud, zona.longitud),
                        radius = zona.radio.toDoubleOrNull() ?: 50.0,
                        fillColor = Color.Red.copy(alpha = 0.2f),
                        strokeColor = Color.Red,
                        strokeWidth = 2f
                    )
                    
                    Marker(
                        state = MarkerState(position = LatLng(zona.latitud, zona.longitud)),
                        title = "Riesgo: ${zona.nivelRiesgo}",
                        snippet = zona.nombre,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                }
                if (uiState.pathPoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.pathPoints.map { LatLng(it.lat, it.lng) },
                        color = when (uiState.selectedMode) {
                            TravelMode.WALKING -> Color(0xFF4CAF50)
                            TravelMode.BICYCLING -> Color(0xFF007AFF)
                            TravelMode.HIKING -> Color(0xFF795548)
                        },
                        width = 12f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                viewModel.setOrigin(it.latitude, it.longitude, "Mi ubicación")
                                viewModel.updateLocation(it.latitude, it.longitude)
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f))
                                }
                            }
                        }
                    } else {
                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Ubicación")
            }

            if (uiState.isGuiding) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            when (uiState.selectedMode) {
                                TravelMode.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
                                TravelMode.BICYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
                                TravelMode.HIKING -> Icons.Default.Terrain
                            },
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(uiState.currentInstruction, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Modo ${uiState.selectedMode.label}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (!uiState.isGuiding) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = uiState.originText,
                            onValueChange = { viewModel.updateOriginText(it) },
                            label = { Text("Punto de Origen") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { findLocation(uiState.originText, true) }) {
                                    Icon(Icons.Default.Search, null)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.destinationText,
                            onValueChange = { viewModel.updateDestinationText(it) },
                            label = { Text("Punto de Destino") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { findLocation(uiState.destinationText, false) }) {
                                    Icon(Icons.Default.Search, null)
                                }
                            }
                        )
                    }
                }

                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TravelMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = uiState.selectedMode == mode,
                                    onClick = { viewModel.setSelectedMode(mode) },
                                    label = { Text(mode.label) },
                                    leadingIcon = {
                                        Icon(
                                            when (mode) {
                                                TravelMode.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
                                                TravelMode.BICYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
                                                TravelMode.HIKING -> Icons.Default.Terrain
                                            },
                                            null
                                        )
                                    }
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.calculateRoute() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Ver Ruta")
                                }
                            }
                            IconButton(
                                onClick = { viewModel.saveRoute() },
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Save, null)
                            }
                        }
                        if (uiState.pathPoints.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.startGuiding() },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("Iniciar Viaje")
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(uiState.estimatedTime, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF2E7D32))
                                Text("${uiState.distanceText} • ${uiState.elevationGain}", color = Color.Gray, fontSize = 14.sp)
                            }
                            Button(
                                onClick = { viewModel.stopGuiding() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Terminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
