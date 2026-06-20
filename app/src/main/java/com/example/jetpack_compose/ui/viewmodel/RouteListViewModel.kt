package com.example.jetpack_compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose.data.model.SafeRoute
import com.example.jetpack_compose.data.repository.RouteRepository
import com.example.jetpack_compose.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RouteListUiState(
    val routes: List<SafeRoute> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = true
)

class RouteListViewModel(private val routeRepository: RouteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteListUiState())
    val uiState: StateFlow<RouteListUiState> = _uiState.asStateFlow()

    init {
        loadRoutes(null)
    }

    fun filterByCategory(categoria: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = categoria, isLoading = true)
        loadRoutes(categoria)
    }

    private fun loadRoutes(categoria: String?) {
        viewModelScope.launch {
            val flow = if (categoria == null) {
                routeRepository.getAllRoutes()
            } else {
                routeRepository.getRoutesByCategory(categoria)
            }
            flow.collect { routes ->
                _uiState.value = RouteListUiState(
                    routes = routes,
                    selectedCategory = categoria,
                    isLoading = false
                )
            }
        }
    }

    fun getCategoryLabel(categoria: String): String = when (categoria) {
        Constants.Categorias.URBANA -> "Urbana"
        Constants.Categorias.CICLISMO -> "Ciclismo"
        Constants.Categorias.SENDERISMO -> "Senderismo"
        else -> categoria
    }

    class Factory(private val routeRepository: RouteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RouteListViewModel(routeRepository) as T
        }
    }
}
