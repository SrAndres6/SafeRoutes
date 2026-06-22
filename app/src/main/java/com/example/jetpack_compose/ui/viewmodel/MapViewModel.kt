package com.example.jetpack_compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose.data.model.SafeRoute
import com.example.jetpack_compose.data.model.Zona
import com.example.jetpack_compose.data.remote.DirectionsService
import com.example.jetpack_compose.data.repository.AuthRepository
import com.example.jetpack_compose.data.repository.RouteRepository
import com.example.jetpack_compose.data.repository.ZoneRepository
import com.example.jetpack_compose.util.Constants
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TravelMode(val value: String, val label: String, val categoria: String) {
    WALKING("walking", "Peatón", Constants.Categorias.URBANA),
    BICYCLING("bicycling", "Ciclista", Constants.Categorias.CICLISMO),
    HIKING("walking", "Senderismo", Constants.Categorias.SENDERISMO)
}

data class LatLngData(val lat: Double, val lng: Double)

data class MapUiState(
    val originText: String = "SENA CTMA Antioquia",
    val destinationText: String = "Doce de Octubre Medellín",
    val selectedMode: TravelMode = TravelMode.BICYCLING,
    val origin: LatLngData? = null,
    val destination: LatLngData? = null,
    val pathPoints: List<LatLngData> = emptyList(),
    val polylineEncoded: String = "",
    val isGuiding: Boolean = false,
    val currentInstruction: String = "",
    val estimatedTime: String = "",
    val distanceText: String = "",
    val distanceMeters: Int = 0,
    val durationMinutes: Int = 0,
    val elevationGain: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val zonas: List<Zona> = emptyList(),
    val communityRoutes: List<SafeRoute> = emptyList(),
    val saveSuccess: Boolean = false
)

class MapViewModel(
    private val directionsService: DirectionsService,
    private val routeRepository: RouteRepository,
    private val zoneRepository: ZoneRepository,
    private val authRepository: AuthRepository,
    private val apiKey: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            zoneRepository.getZonas().collect { zonas ->
                _uiState.value = _uiState.value.copy(zonas = zonas)
            }
        }
        viewModelScope.launch {
            routeRepository.getAllRoutes().collect { routes ->
                _uiState.value = _uiState.value.copy(communityRoutes = routes)
            }
        }
    }

    fun updateOriginText(text: String) {
        _uiState.value = _uiState.value.copy(originText = text)
    }

    fun updateDestinationText(text: String) {
        _uiState.value = _uiState.value.copy(destinationText = text)
    }

    fun setSelectedMode(mode: TravelMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun setOrigin(lat: Double, lng: Double, name: String? = null) {
        _uiState.value = _uiState.value.copy(
            origin = LatLngData(lat, lng),
            originText = name ?: _uiState.value.originText
        )
    }

    fun setDestination(lat: Double, lng: Double, name: String? = null) {
        _uiState.value = _uiState.value.copy(
            destination = LatLngData(lat, lng),
            destinationText = name ?: _uiState.value.destinationText
        )
    }

    fun calculateRoute() {
        val origin = _uiState.value.origin
        val destination = _uiState.value.destination
        if (origin == null || destination == null) {
            _uiState.value = _uiState.value.copy(error = "Selecciona origen y destino")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val originStr = "${origin.lat},${origin.lng}"
                val destStr = "${destination.lat},${destination.lng}"
                val mode = _uiState.value.selectedMode.value
                val response = directionsService.getDirections(originStr, destStr, apiKey, mode)

                if (response.isSuccessful) {
                    val directions = response.body()
                    if (directions?.routes?.isNotEmpty() == true) {
                        val route = directions.routes[0]
                        val points = PolyUtil.decode(route.overview_polyline.points)
                            .map { LatLngData(it.latitude, it.longitude) }
                        val leg = route.legs.getOrNull(0)
                        val distanceMeters = leg?.distance?.value ?: 0
                        val durationMinutes = (leg?.duration?.value ?: 0) / 60
                        val elevation = if (_uiState.value.selectedMode == TravelMode.BICYCLING) {
                            "+${distanceMeters / 100} m"
                        } else ""

                        _uiState.value = _uiState.value.copy(
                            pathPoints = points,
                            polylineEncoded = route.overview_polyline.points,
                            distanceText = leg?.distance?.text ?: "",
                            distanceMeters = distanceMeters,
                            durationMinutes = durationMinutes,
                            estimatedTime = leg?.duration?.text ?: "",
                            elevationGain = elevation,
                            currentInstruction = route.legs[0].steps[0].html_instructions
                                .replace(Regex("<[^>]*>"), ""),
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = directions?.status ?: "Sin rutas disponibles"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error de red")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun saveRoute(descripcion: String = "", dificultad: String = Constants.Dificultad.FACIL) {
        val state = _uiState.value
        val userId = authRepository.currentUserId ?: return
        val origin = state.origin ?: return
        val destination = state.destination ?: return

        viewModelScope.launch {
            val route = SafeRoute(
                nombre = "Ruta a ${state.destinationText}",
                descripcion = descripcion.ifEmpty { "Ruta ${state.selectedMode.label} de ${state.originText} a ${state.destinationText}" },
                categoria = state.selectedMode.categoria,
                distanciaMetros = state.distanceMeters,
                distanciaTexto = state.distanceText,
                tiempoMinutos = state.durationMinutes,
                tiempoTexto = state.estimatedTime,
                dificultad = dificultad,
                origenLat = origin.lat,
                origenLng = origin.lng,
                origenNombre = state.originText,
                destinoLat = destination.lat,
                destinoLng = destination.lng,
                destinoNombre = state.destinationText,
                polyline = state.polylineEncoded,
                modoTransporte = state.selectedMode.value,
                creadorId = userId
            )
            routeRepository.saveRoute(route)
                .onSuccess { _uiState.value = _uiState.value.copy(saveSuccess = true) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun startGuiding() {
        _uiState.value = _uiState.value.copy(isGuiding = true)
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            zoneRepository.sendAlerta(userId, "Iniciando viaje a ${_uiState.value.destinationText}", "VIAJE_INICIADO")
        }
    }

    fun stopGuiding() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            zoneRepository.sendAlerta(userId, "Viaje finalizado en ${_uiState.value.destinationText}", "VIAJE_FINALIZADO")
        }
        _uiState.value = _uiState.value.copy(isGuiding = false, pathPoints = emptyList())
    }

    fun updateLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            if (_uiState.value.isGuiding) {
                zoneRepository.updateUserLocation(userId, lat, lng)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun loadRoute(routeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val route = routeRepository.getRouteById(routeId)
            if (route != null) {
                val points = PolyUtil.decode(route.polyline)
                    .map { LatLngData(it.latitude, it.longitude) }
                
                val mode = TravelMode.entries.find { it.value == route.modoTransporte } 
                    ?: TravelMode.BICYCLING

                _uiState.value = _uiState.value.copy(
                    origin = LatLngData(route.origenLat, route.origenLng),
                    originText = route.origenNombre,
                    destination = LatLngData(route.destinoLat, route.destinoLng),
                    destinationText = route.destinoNombre,
                    pathPoints = points,
                    polylineEncoded = route.polyline,
                    selectedMode = mode,
                    distanceText = route.distanciaTexto,
                    distanceMeters = route.distanciaMetros,
                    durationMinutes = route.tiempoMinutos,
                    estimatedTime = route.tiempoTexto,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Ruta no encontrada")
            }
        }
    }

    class Factory(
        private val directionsService: DirectionsService,
        private val routeRepository: RouteRepository,
        private val zoneRepository: ZoneRepository,
        private val authRepository: AuthRepository,
        private val apiKey: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MapViewModel(directionsService, routeRepository, zoneRepository, authRepository, apiKey) as T
        }
    }
}
