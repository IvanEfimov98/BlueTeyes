package com.connect.blueteyes.utils

import android.content.Context
import android.content.SharedPreferences
import com.connect.blueteyes.model.AppInfo
import com.connect.blueteyes.model.BluetoothDeviceInfo
import com.connect.blueteyes.model.HistoryRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataStoreManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("blueteyes_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SELECTED_APPS = "selected_apps"
        private const val KEY_KNOWN_DEVICES = "known_devices"
        private const val KEY_CONNECTION_HISTORY = "connection_history"
        private const val KEY_NOTIFICATION_ACCESS_ENABLED = "notification_access_enabled"
    }

    // --- Работа со списком выбранных приложений ---
    fun saveSelectedApps(apps: List<AppInfo>) {
        val json = gson.toJson(apps.filter { it.isSelected }.map { it.packageName })
        prefs.edit().putString(KEY_SELECTED_APPS, json).apply()
    }

    fun loadSelectedAppPackages(): Set<String> {
        val json = prefs.getString(KEY_SELECTED_APPS, null) ?: return emptySet()
        val type = object : TypeToken<List<String>>() {}.type
        val list: List<String> = gson.fromJson(json, type) ?: emptyList()
        return list.toSet()
    }

    // --- Работа с известными устройствами (для автоподключения) ---
    fun saveKnownDevices(devices: List<BluetoothDeviceInfo>) {
        val json = gson.toJson(devices)
        prefs.edit().putString(KEY_KNOWN_DEVICES, json).apply()
    }

    fun loadKnownDevices(): MutableList<BluetoothDeviceInfo> {
        val json = prefs.getString(KEY_KNOWN_DEVICES, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<BluetoothDeviceInfo>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    // --- Работа с историей подключений ---
    fun addHistoryRecord(record: HistoryRecord) {
        val currentHistory = loadHistory().toMutableList()
        currentHistory.add(0, record) // Новые записи сверху
        if (currentHistory.size > 50) currentHistory.removeAt(currentHistory.size - 1) // Лимит 50 записей
        saveHistory(currentHistory)
    }

    fun saveHistory(history: List<HistoryRecord>) {
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_CONNECTION_HISTORY, json).apply()
    }

    fun loadHistory(): List<HistoryRecord> {
        val json = prefs.getString(KEY_CONNECTION_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<HistoryRecord>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // --- Работа с переключателем доступа к уведомлениям ---
    fun setNotificationAccessEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ACCESS_ENABLED, enabled).apply()
    }

    fun isNotificationAccessEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ACCESS_ENABLED, false)
    }
}