package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "JarvisAccessibility"
        var instance: JarvisAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "JARVIS Accessibility Service Online & Connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "JARVIS Accessibility Service Destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        // UI event tracking (used to read screen text and interactive states)
        Log.d(TAG, "Intercepted Event: ${event.eventType} from ${event.packageName}")
    }

    override fun onInterrupt() {
        Log.d(TAG, "JARVIS Accessibility Service Interrupted")
    }

    // 1. Perform click at specific screen coordinates (X, Y)
    fun performClick(x: Float, y: Float, onComplete: (() -> Unit)? = null) {
        val clickPath = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(clickPath, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete?.invoke()
            }
        }, null)
    }

    // 2. Perform swipe gesture (for scroll, screen gestures, etc.)
    fun performSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        duration: Long = 300,
        onComplete: (() -> Unit)? = null
    ) {
        val swipePath = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(swipePath, 0, duration)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete?.invoke()
            }
        }, null)
    }

    // 3. Find specific text on screen and perform click (e.g., 'Send' or action buttons)
    fun clickNodeByText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        for (node in nodes) {
            if (node.isClickable) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    parent = parent.parent
                }
            }
        }
        return false
    }

    // 4. Directly type text into the currently focused input field
    fun typeText(textToType: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focusedNode != null) {
            val arguments = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    textToType
                )
            }
            return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return false
    }

    // 5. Navigate to Home Screen
    fun goToHomeScreen() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    // 6. Lock Device Screen
    fun lockScreen() {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    // 7. Trigger System Back Action
    fun pressBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }
}
