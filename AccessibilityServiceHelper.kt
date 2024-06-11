package com.bargarapp.testks

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityServiceHelper : AccessibilityService() {
    private var rootInActiveWindow: AccessibilityNodeInfo? = null
    val filter = IntentFilter("com.bargarapp.testks.ACTIVATE_METHOD")
    val receiver = AccesReceiver()
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {
// Этот метод вызывается системой, когда сервис должен быть прерван
        rootInActiveWindow = null
        Log.i("AccessibilityService", "Service Interrupted")
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        disableSelf()
    }

    @SuppressLint("NewApi")
    override fun onServiceConnected() {
        super.onServiceConnected()
        rootInActiveWindow = getRootInActiveWindow()
        registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        Log.i("AccessibilityService", "Connected")
    }

    fun performSwipeUp(duration: Long) {
        rootInActiveWindow = getRootInActiveWindow()
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        val x = screenWidth / 2
        val startY = screenHeight * 3 / 4 // Start swipe from 3/4th of the height
        val endY = screenHeight / 4 // End swipe at 1/4th of the height

        val path = Path().apply {
            moveTo(x.toFloat(), startY.toFloat())
            lineTo(x.toFloat(), endY.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        this.dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                // Жест выполнен успешно
                Log.d("Gesture", "Gesture completed successfully")
                sendResult("Result: Swipe up completed with duration $duration ms")
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                // Жест был отменен
                Log.d("Gesture", "Gesture was cancelled")
                sendResult("Result: Swipe up canceled with duration $duration ms")
            }
        }, null)

    }

    fun performSwipeDown(duration: Long) {
        rootInActiveWindow = getRootInActiveWindow()
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        val x = screenWidth / 2
        val startY = screenHeight / 4
        val endY = screenHeight * 3 / 4

        val path = Path().apply {
            moveTo(x.toFloat(), startY.toFloat())
            lineTo(x.toFloat(), endY.toFloat())
        }
        Log.d("GesturePath", "Path coordinates: StartX=$x, StartY=$startY, EndX=$x, EndY=$endY")

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        this.dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                // Жест выполнен успешно
                Log.d("Gesture", "Gesture completed successfully")
                sendResult("Result: Swipe down completed with duration $duration ms")
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                // Жест был отменен
                Log.d("Gesture", "Gesture was cancelled")
                sendResult("Result: Swipe down canceled with duration $duration ms")
            }
        }, null)

    }
    private var sendResultCallback: ((String) -> Unit)? = null

    fun setSendResultCallback(callback: (String) -> Unit) {
        sendResultCallback = callback
    }

    private fun sendResult(result: String) {
        sendResultCallback?.invoke(result)
    }
    // BroadcastReceiver
    class AccesReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val duration = intent.getLongExtra("Duration", 1000)
            val direction = intent.getStringExtra("Direction")
            if (direction == "up"){
                (context as AccessibilityServiceHelper).performSwipeUp(duration)
            }
            if (direction == "down") {
                (context as AccessibilityServiceHelper).performSwipeDown(duration)
            }
        }
    }

}