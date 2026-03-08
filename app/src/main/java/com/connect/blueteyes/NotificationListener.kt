package com.connect.blueteyes.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Здесь получаем уведомление
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getString(Notification.EXTRA_TEXT)

        Log.d("NotifListener", "Пакет: $packageName, Заголовок: $title, Текст: $text")

        // TODO: Отправить эти данные по Bluetooth на магнитолу
    }

    override fun onListenerConnected() {
        Log.d("NotifListener", "Слушатель уведомлений подключён")
    }
}