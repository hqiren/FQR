package com.fqrproject.fqr.ui.blocking

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.PixelFormat
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class AppBlockerService : AccessibilityService() {
    private var overlayView: android.view.View? = null
    private var windowManager: WindowManager? = null
    private var blockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Don't block our own app
        if (packageName == this.packageName) {
            removeOverlay()
            return
        }

        val prefs = getSharedPreferences("blocking_state", MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("blocking_enabled", false)
        val blockedAppsJson = prefs.getString("blocked_apps", "[]") ?: "[]"
        val blockedApps = try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(blockedAppsJson)
        } catch (e: Exception) {
            emptyList()
        }

        if (isEnabled && blockedApps.contains(packageName)) {
            if (blockedPackage != packageName) {
                blockedPackage = packageName
                showOverlay()
            }
        } else {
            blockedPackage = null
            removeOverlay()
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return

        performGlobalAction(GLOBAL_ACTION_HOME)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.OPAQUE
        )

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(android.graphics.Color.argb(240, 20, 20, 20))

            addView(android.widget.TextView(this@AppBlockerService).apply {
                text = "APP BLOCKED"
                textSize = 28f
                setTextColor(android.graphics.Color.WHITE)
                gravity = android.view.Gravity.CENTER
            })

            addView(android.widget.TextView(this@AppBlockerService).apply {
                text = "You've exceeded your daily screen time limit!"
                textSize = 16f
                setTextColor(android.graphics.Color.LTGRAY)
                gravity = android.view.Gravity.CENTER
                setPadding(32, 16, 32, 32)
            })

            addView(android.widget.Button(this@AppBlockerService).apply {
                text = "Go Home"
                setOnClickListener {
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(homeIntent)
                    removeOverlay()
                }
            })
        }

        overlayView = layout
        windowManager?.addView(layout, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
        blockedPackage = null
    }

    override fun onInterrupt() {
        removeOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}