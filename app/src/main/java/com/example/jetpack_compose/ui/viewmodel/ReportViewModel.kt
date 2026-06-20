package com.example.jetpack_compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose.data.local.entity.PendingReporteEntity
import com.example.jetpack_compose.data.model.Reporte
import com.example.jetpack_compose.data.model.SafeRoute
import com.example.jetpack_compose.data.repository.AuthRepository
import com.example.jetpack_compose.data.repository.ReportRepository
import com.example.jetpack_compose.data.repository.RouteRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val routes: List<SafeRoute> = emptyList(),
    val selectedRouteId: String = "",
    val selectedRouteName: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val routeRepository: RouteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            routeRepository.getAllRoutes().collect { routes ->
                _uiState.value = _uiState.value.copy(routes = routes)
            }
        }
    }

    fun selectRoute(routeId: String, routeName: String) {
        _uiState.value = _uiState.value.copy(selectedRouteId = routeId, selectedRouteName = routeName)
    }

    fun setTipo(tipo: String) {
        _uiState.value = _uiState.value.copy(tipo = tipo)
    }

    fun setDescripcion(text: String) {
        _uiState.value = _uiState.value.copy(descripcion = text)
    }

    fun setLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(latitud = lat.toString(), longitud = lng.toString())
    }

    fun submitReport(online: Boolean = true) {
        val state = _uiState.value
        if (state.selectedRouteId.isEmpty() || state.tipo.isEmpty() ||
            state.descripcion.isEmpty() || state.latitud.isEmpty() || state.longitud.isEmpty()
        ) {
            _uiState.value = state.copy(error = "Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            val user = authRepository.getCurrentUser()

            val reporte = Reporte(
                rutaId = state.selectedRouteId,
                rutaNombre = state.selectedRouteName,
                tipo = state.tipo,
                descripcion = state.descripcion,
                latitud = state.latitud.toDouble(),
                longitud = state.longitud.toDouble(),
                usuarioId = user?.id ?: "",
                usuarioNombre = user?.name ?: "",
                fecha = Timestamp.now()
            )

            if (online) {
                reportRepository.addReporte(reporte)
                    .onSuccess {
                        _uiState.value = ReportUiState(message = "Reporte enviado correctamente")
                    }
                    .onFailure {
                        saveOffline(reporte, user?.name ?: "")
                    }
            } else {
                saveOffline(reporte, user?.name ?: "")
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    private suspend fun saveOffline(reporte: Reporte, userName: String) {
        reportRepository.saveReporteOffline(
            PendingReporteEntity(
                rutaId = reporte.rutaId,
                rutaNombre = reporte.rutaNombre,
                tipo = reporte.tipo,
                descripcion = reporte.descripcion,
                latitude = reporte.latitud,
                longitude = reporte.longitud,
                userId = reporte.usuarioId,
                userName = userName
            )
        )
        _uiState.value = _uiState.value.copy(message = "Reporte guardado offline. Se sincronizará después.")
    }

    class Factory(
        private val reportRepository: ReportRepository,
        private val routeRepository: RouteRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportViewModel(reportRepository, routeRepository, authRepository) as T
        }
    }
}
