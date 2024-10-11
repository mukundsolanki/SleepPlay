package com.example.sleepplay

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager

class ScreenControlService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "TURN_OFF_SCREEN" -> turnOffScreen()
            "TURN_ON_SCREEN" -> turnOnScreen()
        }
        return START_NOT_STICKY
    }

    private fun turnOffScreen() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (powerManager.isInteractive) {
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "SleepPlay:ScreenOffWakeLock"
            )
            wakeLock.acquire(10*60*1000L) // 10 minutes timeout
            wakeLock.release()
        }
    }

    private fun turnOnScreen() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "SleepPlay:ScreenOnWakeLock"
        )
        wakeLock.acquire(1000L) // 1 second timeout
        wakeLock.release()
    }
}