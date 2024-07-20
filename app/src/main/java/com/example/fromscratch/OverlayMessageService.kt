package com.example.fromscratch
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.os.Handler
import android.os.Looper

class OverlayMessageService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private val lock = Any()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("message") ?: return START_NOT_STICKY
        showOverlayMessage(message)
        return START_NOT_STICKY
    }

    private fun showOverlayMessage(message: String) {
        synchronized(lock) {
            if (overlayView == null) {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
                params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                params.y = 100

                overlayView = TextView(this).apply {
                    setBackgroundColor(0x80000000.toInt())
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(30, 30, 30, 30)
                }

                try {
                    windowManager.addView(overlayView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }
            }

            overlayView?.text = message

            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                removeOverlayView()
            }, 3000)
        }
    }

    private fun removeOverlayView() {
        synchronized(lock) {
            overlayView?.let {
                try {
                    windowManager.removeView(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                overlayView = null
            }
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayView()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}