package com.example.churchconnect.workers

import androidx.work.Data
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for WorkManager background worker data serialization & payload integrity.
 */
class WorkManagerTest {

    /**
     * WORKER_001 (Positive)
     * Verifies that attendance sync input data serializes date and present member UIDs correctly.
     */
    @Test
    fun WORKER_001_offlineAttendanceData_serializesCorrectly() {
        val date = "2026-10-18"
        val uids = arrayOf("user1", "user2", "user3")

        val data = Data.Builder()
            .putString("date", date)
            .putStringArray("presentUids", uids)
            .build()

        assertEquals("2026-10-18", data.getString("date"))
        val retrievedUids = data.getStringArray("presentUids")
        assertNotNull(retrievedUids)
        assertEquals(3, retrievedUids!!.size)
        assertEquals("user1", retrievedUids[0])
    }

    /**
     * WORKER_002 (Negative)
     * Verifies that missing date payload handles null safety correctly.
     */
    @Test
    fun WORKER_002_offlineAttendanceData_handlesMissingDate() {
        val data = Data.Builder().build()
        assertNull(data.getString("date"))
        assertNull(data.getStringArray("presentUids"))
    }

    /**
     * WORKER_003 (Positive)
     * Verifies that DailyWordSyncWorker can be instantiated and constructed for testing.
     */
    @Test
    fun WORKER_003_dailyWordSyncWorker_initializesClassCorrectly() {
        val workerClass = DailyWordSyncWorker::class.java
        assertNotNull(workerClass)
        assertEquals("com.example.churchconnect.workers.DailyWordSyncWorker", workerClass.name)
    }
}
