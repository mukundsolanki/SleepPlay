package com.example.sleepplay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.view.WindowInsetsController
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class BlackScreenService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout
    private lateinit var timeTextView: TextView
    private var isTimeDisplayEnabled = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeUpdateRunnable: Runnable

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupTimeUpdateRunnable()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "SHOW_BLACK_SCREEN" -> showBlackScreen()
            "HIDE_BLACK_SCREEN" -> hideBlackScreen()
            "ENABLE_TIME_DISPLAY" -> {
                isTimeDisplayEnabled = true
                updateTimeDisplay()
            }
            "DISABLE_TIME_DISPLAY" -> {
                isTimeDisplayEnabled = false
                updateTimeDisplay()
            }
            "STOP_OVERLAY" -> {
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun setupTimeUpdateRunnable() {
        timeUpdateRunnable = object : Runnable {
            override fun run() {
                updateTimeDisplay()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun showBlackScreen() {
        if (!::overlayView.isInitialized) {
            overlayView = FrameLayout(this).apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@BlackScreenService,
                        android.R.color.black
                    )
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    windowInsetsController?.apply {
                        hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
                        systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                }

                timeTextView = TextView(context).apply {
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    textSize = 40f
                    gravity = Gravity.CENTER
                    visibility = View.GONE
                }
                addView(timeTextView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                })
            }

            val params = WindowManager.LayoutParams().apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                format = PixelFormat.OPAQUE
                gravity = Gravity.TOP or Gravity.START
                screenBrightness = 0f
            }

            windowManager.addView(overlayView, params)
            handler.post(timeUpdateRunnable)
        }
    }

    private fun hideBlackScreen() {
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
            handler.removeCallbacks(timeUpdateRunnable)
        }
    }

    private fun updateTimeDisplay() {
        if (::timeTextView.isInitialized) {
            if (isTimeDisplayEnabled) {
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val currentTime = sdf.format(Date())
                timeTextView.text = currentTime
                timeTextView.visibility = View.VISIBLE
            } else {
                timeTextView.visibility = View.GONE
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        hideBlackScreen()
        handler.removeCallbacks(timeUpdateRunnable)
    }
}