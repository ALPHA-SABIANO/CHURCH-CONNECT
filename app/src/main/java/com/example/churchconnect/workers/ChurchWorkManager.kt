package com.example.churchconnect.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ChurchWorkManager {

    private const val DAILY_WORD_SYNC_TAG = "church_daily_word_sync"

    /**
     * Schedules periodic background sync of Daily Word of Encouragement (runs every 12 hours).
     */
    fun scheduleDailyWordSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<DailyWordSyncWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORD_SYNC_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    /**
     * Schedules a one-time network-constrained worker to sync offline attendance records.
     */
    fun scheduleOfflineAttendanceSync(context: Context, date: String, presentUids: List<String>) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncData = workDataOf(
            "date" to date,
            "presentUids" to presentUids.toTypedArray()
        )

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<OfflineAttendanceWorker>()
            .setConstraints(constraints)
            .setInputData(syncData)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
    }

    /**
     * Triggers an immediate execution of DailyWordSyncWorker (useful for instant testing).
     */
    fun runDailyWordSyncNow(context: Context) {
        val immediateWorkRequest = OneTimeWorkRequestBuilder<DailyWordSyncWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(immediateWorkRequest)
    }
}
