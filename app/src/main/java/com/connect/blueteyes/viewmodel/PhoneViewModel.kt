package com.connect.blueteyes.viewmodel

import android.app.Application
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.connect.blueteyes.service.BluetoothServer
import com.connect.blueteyes.service.NotificationListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.io.IOException

class PhoneViewModel(application: Application) : AndroidViewModel(application) {
    private var bluetoothServer: BluetoothServer? = null
    private var currentSocket: BluetoothSocket? = null
    private var outputStream: DataOutputStream? = null

    init {
        // Регистрируем колбэк для получения уведомлений
        NotificationListener.onNotificationPostedListener = { packageName, text ->
            // Отправляем текст уведомления на магнитолу
            sendNotificationToHeadUnit(text)
        }
    }

    fun startServer() {
        bluetoothServer = BluetoothServer { socket ->
            // Когда клиент подключается
            currentSocket = socket
            try {
                outputStream = DataOutputStream(socket.outputStream)
                Log.d("PhoneViewModel", "Клиент подключён")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        bluetoothServer?.start()
    }

    fun stopServer() {
        bluetoothServer?.stop()
        try {
            outputStream?.close()
            currentSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        bluetoothServer = null
        currentSocket = null
        outputStream = null
    }

    private fun sendNotificationToHeadUnit(text: String?) {
        if (text == null) return
        if (outputStream == null) {
            Log.e("PhoneViewModel", "Нет подключения к магнитоле")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                outputStream?.writeUTF(text)  // отправляем строку
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun openNotificationAccess() {
        val context = getApplication<Application>()
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            fallbackIntent.data = Uri.parse("package:${context.packageName}")
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
        NotificationListener.onNotificationPostedListener = null
    }
}