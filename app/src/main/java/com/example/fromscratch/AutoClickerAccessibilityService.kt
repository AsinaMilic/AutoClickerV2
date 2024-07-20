package com.example.fromscratch

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler
import android.os.Looper
import android.content.SharedPreferences
import android.content.Intent

class AutoClickerAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sharedPreferences: SharedPreferences
    private var isReading = false

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("AutoClickerPrefs", MODE_PRIVATE)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isReading = true
        startReading()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Ovaj metod će biti pozvan za svaki accessibility event,
        // ali mi ćemo koristiti našu vlastitu logiku za čitanje
    }

    override fun onInterrupt() {
        isReading = false
    }

    private fun startReading() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isReading && isServiceEnabled()) {
                    val rootNode = rootInActiveWindow
                    if (rootNode != null) {
                        val content = getAllText(rootNode)
                        showOverlayMessage(content)
                    }
                }
                handler.postDelayed(this, 2000) // Čitaj svakih 2 sekunde
            }
        }, 0)
    }

    private fun getAllText(node: AccessibilityNodeInfo): String {
        val stringBuilder = StringBuilder()
        if (node.text != null) {
            stringBuilder.append(node.text)
            stringBuilder.append("\n")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            stringBuilder.append(getAllText(child))
        }
        return stringBuilder.toString()
    }

    private fun showOverlayMessage(message: String) {
        val intent = Intent(this, OverlayMessageService::class.java).apply {
            putExtra("message", message)
        }
        startService(intent)
    }

    private fun isServiceEnabled(): Boolean {
        return sharedPreferences.getBoolean("service_enabled", false)
    }

    companion object {
        fun setServiceEnabled(context: android.content.Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences("AutoClickerPrefs", MODE_PRIVATE)
            prefs.edit().putBoolean("service_enabled", enabled).apply()
        }
    }
}