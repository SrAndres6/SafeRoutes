package com.example.jetpack_compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose.data.model.SafeRoute
import com.example.jetpack_compose.data.model.User
import com.example.jetpack_compose.data.repository.AuthRepository
import com.example.jetpack_compose.data.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val favorites: List<SafeRoute> = emptyList(),
    val isLoading: Boolean = true
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.value = _uiState.value.copy(user = user, isLoading = false)
            user?.id?.let { userId ->
                routeRepository.getFavorites(userId).collect { favs ->
                    _uiState.value = _uiState.value.copy(favorites = favs)
                }
            }
        }
    }

    fun logout() = authRepository.logout()

    class Factory(
        private val authRepository: AuthRepository,
        private val routeRepository: RouteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(authRepository, routeRepository) as T
        }
    }
}
