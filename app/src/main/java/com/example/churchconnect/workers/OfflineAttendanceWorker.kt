package com.example.churchconnect.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.churchconnect.ChurchRepository

/**
 * Background WorkManager worker that retries saving member attendance records
 * automatically when network connection is restored.
 */
class OfflineAttendanceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val date = inputData.getString("date") ?: return Result.failure()
        val presentUids = inputData.getStringArray("presentUids")?.toList() ?: emptyList()

        return try {
            ChurchRepository.saveAttendance(date, presentUids)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
