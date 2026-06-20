package com.example.jetpack_compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose.data.model.SafeRoute
import com.example.jetpack_compose.data.repository.AuthRepository
import com.example.jetpack_compose.data.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "...",
    val topRoutes: List<SafeRoute> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.value = _uiState.value.copy(userName = user?.name ?: "Usuario")
        }
        viewModelScope.launch {
            routeRepository.getAllRoutes().collect { routes ->
                _uiState.value = _uiState.value.copy(
                    topRoutes = routes.take(3),
                    isLoading = false
                )
            }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val routeRepository: RouteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(authRepository, routeRepository) as T
        }
    }
}
