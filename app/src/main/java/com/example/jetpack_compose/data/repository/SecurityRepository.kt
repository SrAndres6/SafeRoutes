package com.example.jetpack_compose.data.repository

import com.example.jetpack_compose.data.local.dao.IncidentDao
import com.example.jetpack_compose.data.local.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

class SecurityRepository(private val incidentDao: IncidentDao) {

    // Obtener reportes locales (Offline)
    val allIncidents: Flow<List<IncidentEntity>> = incidentDao.getAllIncidents()

    suspend fun addIncident(incident: IncidentEntity) {
        incidentDao.insertIncident(incident)
        // Aquí se agregaría la lógica para sincronizar con Firebase
    }
}
