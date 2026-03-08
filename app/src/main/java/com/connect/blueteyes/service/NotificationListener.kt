package com.connect.blueteyes.service

import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    companion object {
        var onNotificationPostedListener: ((String, String?) -> Unit)? = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras: Bundle = sbn.notification.extras
        val title = extras.getString(android.app.Notification.EXTRA_TITLE)
        val text = extras.getString(android.app.Notification.EXTRA_TEXT)

        Log.d("NotifListener", "Пакет: $packageName, Title: $title, Text: $text")

        // Вызываем колбэк, если он установлен (передадим текст уведомления)
        onNotificationPostedListener?.invoke(packageName, text)
    }

    override fun onListenerConnected() {
        Log.d("NotifListener", "Слушатель уведомлений подключён")
    }
}