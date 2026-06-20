package com.example.jetpack_compose

import android.app.Application
import com.example.jetpack_compose.data.local.PendingReportLocalStore
import com.example.jetpack_compose.data.repository.AuthRepository
import com.example.jetpack_compose.data.repository.ReportRepository
import com.example.jetpack_compose.data.repository.RouteRepository
import com.example.jetpack_compose.data.repository.SecurityRepository
import com.example.jetpack_compose.data.repository.ZoneRepository

class SafeRouteApplication : Application() {

    lateinit var authRepository: AuthRepository
        private set
    lateinit var routeRepository: RouteRepository
        private set
    lateinit var reportRepository: ReportRepository
        private set
    lateinit var zoneRepository: ZoneRepository
        private set
    lateinit var securityRepository: SecurityRepository
        private set
    lateinit var pendingReportLocalStore: PendingReportLocalStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        pendingReportLocalStore = PendingReportLocalStore(applicationContext)

        authRepository = AuthRepository()
        routeRepository = RouteRepository()
        reportRepository = ReportRepository(localStore = pendingReportLocalStore)
        zoneRepository = ZoneRepository()
        securityRepository = SecurityRepository(pendingReportLocalStore)
    }

    companion object {
        lateinit var instance: SafeRouteApplication
            private set
    }
}
