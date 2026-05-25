package com.example.jetpack_compose.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.jetpack_compose.data.local.dao.IncidentDao
import com.example.jetpack_compose.data.local.entity.IncidentEntity

@Database(entities = [IncidentEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
}
