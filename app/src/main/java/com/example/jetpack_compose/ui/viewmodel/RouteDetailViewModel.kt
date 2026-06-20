package com.example.jetpack_compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose.data.model.Reporte
import com.example.jetpack_compose.data.model.SafeRoute
import com.example.jetpack_compose.data.model.Valoracion
import com.example.jetpack_compose.data.repository.AuthRepository
import com.example.jetpack_compose.data.repository.ReportRepository
import com.example.jetpack_compose.data.repository.RouteRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RouteDetailUiState(
    val route: SafeRoute? = null,
    val valoraciones: List<Valoracion> = emptyList(),
    val reportes: List<Reporte> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null
)

class RouteDetailViewModel(
    private val routeId: String,
    private val routeRepository: RouteRepository,
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteDetailUiState())
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()

    init {
        loadRoute()
        loadValoraciones()
        loadReportes()
        checkFavorite()
    }

    private fun loadRoute() {
        viewModelScope.launch {
            val route = routeRepository.getRouteById(routeId)
            _uiState.value = _uiState.value.copy(route = route, isLoading = false)
        }
    }

    private fun loadValoraciones() {
        viewModelScope.launch {
            routeRepository.getValoraciones(routeId).collect { list ->
                _uiState.value = _uiState.value.copy(valoraciones = list)
            }
        }
    }

    private fun loadReportes() {
        viewModelScope.launch {
            reportRepository.getReportesByRoute(routeId).collect { list ->
                _uiState.value = _uiState.value.copy(reportes = list)
            }
        }
    }

    private fun checkFavorite() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val isFav = routeRepository.isFavorite(userId, routeId)
            _uiState.value = _uiState.value.copy(isFavorite = isFav)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val route = _uiState.value.route ?: return@launch
            routeRepository.toggleFavorite(userId, routeId, route)
                .onSuccess { isFav ->
                    _uiState.value = _uiState.value.copy(
                        isFavorite = isFav,
                        message = if (isFav) "Ruta agregada a favoritos" else "Ruta eliminada de favoritos"
                    )
                }
        }
    }

    fun submitRating(puntuacion: Int, comentario: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            val valoracion = Valoracion(
                usuarioId = user.id,
                usuarioNombre = user.name,
                puntuacion = puntuacion,
                comentario = comentario,
                fecha = Timestamp.now()
            )
            routeRepository.addValoracion(routeId, valoracion)
                .onSuccess {
                    loadRoute()
                    _uiState.value = _uiState.value.copy(message = "Valoración enviada")
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(message = "Error: ${it.message}")
                }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    class Factory(
        private val routeId: String,
        private val routeRepository: RouteRepository,
        private val reportRepository: ReportRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RouteDetailViewModel(routeId, routeRepository, reportRepository, authRepository) as T
        }
    }
}
