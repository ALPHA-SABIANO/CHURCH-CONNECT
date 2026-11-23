package com.example.churchconnect

import android.app.Application
import com.example.churchconnect.workers.ChurchWorkManager

class ChurchConnectApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ChurchRepository.init(this)
        try {
            ChurchWorkManager.scheduleDailyWordSync(this)
        } catch (e: Exception) {
            // Safe fallback if WorkManager initialization is delayed
        }
    }
}
