package com.bargarapp.testks

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityServiceHelper : AccessibilityService() {
    companion object {
        private var instance: AccessibilityServiceHelper? = null

        fun getInstance(): AccessibilityServiceHelper {
            return instance ?: synchronized(this) {
                instance ?: AccessibilityServiceHelper().also { instance = it }
            }
        }
    }
    private var rootInActiveWindow: AccessibilityNodeInfo? = null
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {
        rootInActiveWindow = null
        Log.i("AccessibilityService", "Service Interrupted")
        disableSelf()
    }

    @SuppressLint("NewApi")
    override fun onServiceConnected() {
        super.onServiceConnected()
        rootInActiveWindow = getRootInActiveWindow()
        instance = this
        Log.i("AccessibilityService", "Connected")
    }

    fun performSwipeUp(duration: Long) {
        rootInActiveWindow = getRootInActiveWindow()
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        val x = screenWidth / 2
        val startY = screenHeight * 3 / 4
        val endY = screenHeight / 4

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

    fun sendResult(result: String) {
        sendResultCallback?.invoke(result)
        MainActivity.instance.sendResultToServerAfterGestures(result)
        Log.d("AccessibilityServiceHelper", "sendResult: $result")
    }

}