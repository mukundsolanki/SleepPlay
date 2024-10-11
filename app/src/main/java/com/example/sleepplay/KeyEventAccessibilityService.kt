package com.example.sleepplay

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.content.Intent

class KeyEventAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    return true
                }

                KeyEvent.ACTION_UP -> {
                    val intent = Intent(this, BlackScreenService::class.java).apply {
                        action = "STOP_OVERLAY"
                    }
                    startService(intent)

                    val stopAppIntent = Intent("STOP_APP")
                    sendBroadcast(stopAppIntent)

                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }
}