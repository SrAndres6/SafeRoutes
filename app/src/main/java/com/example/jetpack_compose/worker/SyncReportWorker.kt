package com.example.jetpack_compose.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jetpack_compose.SafeRouteApplication

class SyncReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as SafeRouteApplication
            app.reportRepository.syncPendingReportes()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
