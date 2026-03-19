package com.connect.blueteyes.viewmodel

import android.app.Application
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.connect.blueteyes.model.AppInfo
import com.connect.blueteyes.model.HistoryRecord
import com.connect.blueteyes.service.BluetoothServer
import com.connect.blueteyes.service.NotificationListener
import com.connect.blueteyes.utils.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.io.IOException

class PhoneViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    private val dataStore = DataStoreManager(context)

    // Состояние Bluetooth-сервера
    private var bluetoothServer: BluetoothServer? = null
    private var currentSocket: BluetoothSocket? = null
    private var outputStream: DataOutputStream? = null

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    // Список всех установленных приложений
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    // История подключений
    private val _connectionHistory = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val connectionHistory: StateFlow<List<HistoryRecord>> = _connectionHistory.asStateFlow()

    init {
        loadInstalledApps()
        loadHistory()
        setupNotificationListener()
    }

    // Загружаем список приложений, которые могут показывать уведомления
    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val selectedPackages = dataStore.loadSelectedAppPackages()

            val apps = resolveInfos.map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                AppInfo(
                    packageName = packageName,
                    appName = appName,
                    isSelected = packageName in selectedPackages,
                    icon = icon
                )
            }.sortedBy { it.appName }

            _allApps.value = apps
        }
    }

    // Сохраняем выбор приложений
    fun updateAppSelection(app: AppInfo, selected: Boolean) {
        val updatedList = _allApps.value.map {
            if (it.packageName == app.packageName) it.copy(isSelected = selected) else it
        }
        _allApps.value = updatedList
        dataStore.saveSelectedApps(updatedList)
    }

    // Загружаем историю
    private fun loadHistory() {
        _connectionHistory.value = dataStore.loadHistory()
    }

    // Добавляем запись в историю
    fun addConnectionRecord(deviceName: String, deviceAddress: String) {
        val record = HistoryRecord(deviceName, deviceAddress, System.currentTimeMillis())
        val currentList = _connectionHistory.value.toMutableList()
        currentList.add(0, record)
        if (currentList.size > 50) currentList.removeAt(currentList.size - 1)
        _connectionHistory.value = currentList
        dataStore.saveHistory(currentList)
    }

    // Настройка колбэка от сервиса уведомлений
    private fun setupNotificationListener() {
        NotificationListener.onNotificationPostedListener = { packageName, text ->
            // Проверяем, выбрано ли это приложение
            val isSelected = _allApps.value.find { it.packageName == packageName }?.isSelected ?: false
            if (isSelected && text != null) {
                sendNotificationToHeadUnit(text)
            }
        }
    }

    // Отправка текста на магнитолу
    private fun sendNotificationToHeadUnit(text: String) {
        if (outputStream == null) {
            Log.e("PhoneViewModel", "Нет подключения к магнитоле")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                outputStream?.writeUTF(text)
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Управление сервером
    fun startServer() {
        bluetoothServer = BluetoothServer { socket ->
            currentSocket = socket
            try {
                outputStream = DataOutputStream(socket.outputStream)
                Log.d("PhoneViewModel", "Клиент подключён")
                val device = socket.remoteDevice
                addConnectionRecord(device.name ?: "Неизвестно", device.address)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        bluetoothServer?.start()
        _isServerRunning.value = true
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
        _isServerRunning.value = false
    }

    // Открытие настроек доступа к уведомлениям
    fun openNotificationAccess() {
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