package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class JarvisNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "JarvisNotificationRelay"
        var onNotificationReceived: ((sender: String, message: String, appPackage: String) -> Unit)? = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val notification = sbn?.notification ?: return
        val packageName = sbn.packageName ?: return
        val extras = notification.extras ?: return

        // Extract title (sender name) and text (message content)
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isNotEmpty() && text.isNotEmpty()) {
            Log.d(TAG, "Notification received from $packageName: $title - $text")
            onNotificationReceived?.invoke(title, text, packageName)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "JARVIS Notification Relay Connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "JARVIS Notification Relay Disconnected")
    }
}
