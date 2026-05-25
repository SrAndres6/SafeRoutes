package com.example.jetpack_compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose.data.local.entity.IncidentEntity
import com.example.jetpack_compose.data.repository.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val repository: SecurityRepository) : ViewModel() {

    private val _incidents = MutableStateFlow<List<IncidentEntity>>(emptyList())
    val incidents: StateFlow<List<IncidentEntity>> = _incidents

    init {
        viewModelScope.launch {
            repository.allIncidents.collect { list ->
                _incidents.value = list
            }
        }
    }

    fun reportNewIncident(type: String, lat: Double, lng: Double, desc: String) {
        viewModelScope.launch {
            val newIncident = IncidentEntity(
                type = type,
                latitude = lat,
                longitude = lng,
                description = desc
            )
            repository.addIncident(newIncident)
        }
    }
}
