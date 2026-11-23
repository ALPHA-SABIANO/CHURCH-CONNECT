package com.example.churchconnect.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.churchconnect.ChurchRepository
import com.example.churchconnect.MyFirebaseMessagingService
import com.example.churchconnect.publishedWords
import kotlinx.coroutines.flow.firstOrNull

/**
 * Background WorkManager worker that periodically checks for new Words of Encouragement
 * and triggers a local notification even when the app is closed.
 */
class DailyWordSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val words = ChurchRepository.words().firstOrNull() ?: emptyList()
            val latestWord = publishedWords(words, System.currentTimeMillis()).firstOrNull()

            if (latestWord != null) {
                MyFirebaseMessagingService.showNotification(
                    appContext,
                    "Word of Today: ${latestWord.title}",
                    latestWord.scripture.ifBlank { latestWord.message }
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
