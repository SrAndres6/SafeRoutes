package com.example.jetpack_compose.data.local

import android.content.Context
import com.example.jetpack_compose.data.local.entity.PendingReporteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class PendingReportLocalStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _pending = MutableStateFlow(loadFromDisk())
    val pending: Flow<List<PendingReporteEntity>> = _pending.asStateFlow()

    suspend fun insert(reporte: PendingReporteEntity) = withContext(Dispatchers.IO) {
        val current = _pending.value.toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
        current.add(reporte.copy(id = newId))
        save(current)
    }

    suspend fun update(reporte: PendingReporteEntity) = withContext(Dispatchers.IO) {
        val current = _pending.value.map {
            if (it.id == reporte.id) reporte else it
        }
        save(current)
    }

    suspend fun getUnsynced(): List<PendingReporteEntity> = withContext(Dispatchers.IO) {
        _pending.value.filter { !it.synced }
    }

    private fun save(list: List<PendingReporteEntity>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_REPORTS, array.toString()).apply()
        _pending.value = list
    }

    private fun loadFromDisk(): List<PendingReporteEntity> {
        val raw = prefs.getString(KEY_REPORTS, null) ?: return emptyList()
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                add(fromJson(array.getJSONObject(i)))
            }
        }
    }

    private fun fromJson(json: JSONObject): PendingReporteEntity = PendingReporteEntity(
        id = json.getInt("id"),
        rutaId = json.optString("rutaId"),
        rutaNombre = json.optString("rutaNombre"),
        tipo = json.optString("tipo"),
        descripcion = json.optString("descripcion"),
        latitude = json.getDouble("latitude"),
        longitude = json.getDouble("longitude"),
        userId = json.optString("userId"),
        userName = json.optString("userName"),
        synced = json.optBoolean("synced"),
        timestamp = json.optLong("timestamp")
    )

    private fun PendingReporteEntity.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("rutaId", rutaId)
        put("rutaNombre", rutaNombre)
        put("tipo", tipo)
        put("descripcion", descripcion)
        put("latitude", latitude)
        put("longitude", longitude)
        put("userId", userId)
        put("userName", userName)
        put("synced", synced)
        put("timestamp", timestamp)
    }

    companion object {
        private const val PREFS_NAME = "saferoute_pending_reportes"
        private const val KEY_REPORTS = "reports"
    }
}

