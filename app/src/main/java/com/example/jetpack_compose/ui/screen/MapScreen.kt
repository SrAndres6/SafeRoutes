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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.jetpack_compose.data.remote.DirectionsService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

enum class TravelMode(val value: String, val label: String) {
    WALKING("walking", "Peatón"),
    BICYCLING("bicycling", "Ciclista")
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    onNavigateToFriends: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Retrofit Instance
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val directionsService = remember { retrofit.create(DirectionsService::class.java) }
    
    val googleApiKey = "AIzaSyA1tWaQq6A5wVnHUUha_fJXsVomq4NRD1I"
    
    var originText by remember { mutableStateOf("SENA CTMA Antioquia") }
    var destinationText by remember { mutableStateOf("Doce de Octubre Medellín") }
    var selectedMode by remember { mutableStateOf(TravelMode.BICYCLING) }
    
    var originLatLng by remember { mutableStateOf<LatLng?>(null) }
    var destinationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var pathPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var isGuiding by remember { mutableStateOf(false) }
    var currentInstruction by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var distanceText by remember { mutableStateOf("") }
    var elevationGain by remember { mutableStateOf("") }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(6.2442, -75.5812), 12f)
    }

    fun enviarAlertaFirestore(mensaje: String, tipo: String) {
        val uid = auth.currentUser?.uid ?: return
        val alerta = hashMapOf(
            "fecha" to Timestamp.now(),
            "mensaje" to mensaje,
            "tipo" to tipo,
            "usuarioId" to uid
        )
        db.collection("alertas").add(alerta)
    }

    fun guardarRuta() {
        val uid = auth.currentUser?.uid ?: return
        if (originLatLng == null || destinationLatLng == null) return
        
        val ruta = hashMapOf(
            "nombre" to "Ruta a ${destinationText}",
            "origen" to originText,
            "destino" to destinationText,
            "modo" to selectedMode.value,
            "fecha" to Timestamp.now(),
            "usuarioId" to uid
        )
        db.collection("rutas").add(ruta)
            .addOnSuccessListener { Toast.makeText(context, "Ruta guardada", Toast.LENGTH_SHORT).show() }
    }

    fun calculateRoute() {
        if (originLatLng == null || destinationLatLng == null) {
            Toast.makeText(context, "Selecciona origen y destino", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val origin = "${originLatLng!!.latitude},${originLatLng!!.longitude}"
                val destination = "${destinationLatLng!!.latitude},${destinationLatLng!!.longitude}"
                val response = directionsService.getDirections(origin, destination, googleApiKey, selectedMode.value)
                
                if (response.isSuccessful) {
                    val directions = response.body()
                    if (directions?.routes?.isNotEmpty() == true) {
                        val route = directions.routes[0]
                        pathPoints = PolyUtil.decode(route.overview_polyline.points)
                        val leg = route.legs.getOrNull(0)
                        distanceText = leg?.distance?.text ?: ""
                        estimatedTime = leg?.duration?.text ?: ""
                        
                        if (selectedMode == TravelMode.BICYCLING) {
                            val gain = (leg?.distance?.value ?: 0) / 100
                            elevationGain = "+$gain m"
                        } else {
                            elevationGain = ""
                        }
                        
                        currentInstruction = route.legs[0].steps[0].html_instructions.replace(Regex("<[^>]*>"), "")
                        
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.builder().target(originLatLng!!).zoom(17f).tilt(45f).build()
                            )
                        )
                    } else {
                        Toast.makeText(context, "Error: ${directions?.status}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun findLocation(query: String, isOrigin: Boolean) {
        if (query.isEmpty()) return
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val latLng = LatLng(addr.latitude, addr.longitude)
                if (isOrigin) originLatLng = latLng else destinationLatLng = latLng
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        } catch (e: IOException) { }
    }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasLocationPermission = it }
    )

    LaunchedEffect(Unit) {
        findLocation(originText, true)
        findLocation(destinationText, false)
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
                originLatLng?.let { Marker(state = MarkerState(position = it), title = "Origen", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) }
                destinationLatLng?.let { Marker(state = MarkerState(position = it), title = "Destino", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) }
                if (pathPoints.isNotEmpty()) {
                    Polyline(
                        points = pathPoints,
                        color = if (selectedMode == TravelMode.WALKING) Color(0xFF4CAF50) else Color(0xFF007AFF),
                        width = 12f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )
                }
            }

            // Botón Mi Ubicación
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val userPos = LatLng(it.latitude, it.longitude)
                                originLatLng = userPos
                                originText = "Mi ubicación"
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userPos, 16f))
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

            if (isGuiding) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (selectedMode == TravelMode.WALKING) Icons.AutoMirrored.Filled.DirectionsWalk else Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(currentInstruction, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Modo ${selectedMode.label}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (!isGuiding) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = originText,
                            onValueChange = { originText = it },
                            label = { Text("Punto de Origen") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { findLocation(originText, true) }) { Icon(Icons.Default.Search, null) } }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = destinationText,
                            onValueChange = { destinationText = it },
                            label = { Text("Punto de Destino") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { findLocation(destinationText, false) }) { Icon(Icons.Default.Search, null) } }
                        )
                    }
                }

                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FilterChip(selected = selectedMode == TravelMode.WALKING, onClick = { selectedMode = TravelMode.WALKING }, label = { Text("Peatón") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null) })
                            FilterChip(selected = selectedMode == TravelMode.BICYCLING, onClick = { selectedMode = TravelMode.BICYCLING }, label = { Text("Ciclista") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsBike, null) })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { calculateRoute() }, modifier = Modifier.weight(1f)) { Text("Ver Ruta") }
                            IconButton(onClick = { guardarRuta() }, modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))) { Icon(Icons.Default.Save, null) }
                        }
                        if (pathPoints.isNotEmpty()) {
                            Button(
                                onClick = { 
                                    isGuiding = true
                                    enviarAlertaFirestore("Iniciando viaje a ${destinationText}", "VIAJE_INICIADO")
                                },
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
                                Text(estimatedTime, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF2E7D32))
                                Text("$distanceText • $elevationGain", color = Color.Gray, fontSize = 14.sp)
                            }
                            Button(
                                onClick = { 
                                    isGuiding = false 
                                    pathPoints = emptyList()
                                    enviarAlertaFirestore("Viaje finalizado en ${destinationText}", "VIAJE_FINALIZADO")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error)
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
