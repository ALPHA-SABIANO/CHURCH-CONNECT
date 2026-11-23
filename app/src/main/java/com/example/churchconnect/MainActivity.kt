package com.example.churchconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied handling if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        askNotificationPermission()
        initFcm()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = ChurchBlue,
                    onPrimary = Color.White,
                    primaryContainer = SoftBlue,
                    secondary = ChurchGold,
                    tertiary = ChurchGreen,
                    background = ScreenBg,
                    surface = Color.White
                )
            ) {
                AppNav()
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun initFcm() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("church_updates")
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                ChurchRepository.updateFcmToken(token)
                            } catch (e: Exception) {
                                // ignore if offline
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Prevent FCM or Play Services initialization error from crashing the app on startup
        }
    }

    override fun onStop() {
        super.onStop()
        // Sign out when the user leaves or closes the app so the login screen appears on next launch
        try {
            ChurchRepository.logout()
        } catch (e: Exception) {
            // ignore if offline or unauthenticated
        }
    }
}
