package com.connect.blueteyes.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.connect.blueteyes.model.BluetoothDeviceInfo
import com.connect.blueteyes.service.BluetoothClient
import com.connect.blueteyes.tts.TTSManager
import com.connect.blueteyes.utils.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.IOException

class HeadUnitViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val dataStore = DataStoreManager(context)
    private val ttsManager = TTSManager(context)

    private var bluetoothClient: BluetoothClient? = null
    private var currentSocket: BluetoothSocket? = null
    private var inputStream: DataInputStream? = null
    private var receiveJob: Job? = null

    private val _allDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val allDevices: StateFlow<List<BluetoothDevice>> = _allDevices.asStateFlow()

    private val _knownDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val knownDevices: StateFlow<List<BluetoothDeviceInfo>> = _knownDevices.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Отключено")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        loadKnownDevices()
    }

    private fun loadKnownDevices() {
        _knownDevices.value = dataStore.loadKnownDevices()
    }

    fun scanDevices() {
        _isScanning.value = true
        val devices = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        _allDevices.value = devices
        _isScanning.value = false
    }

    // Получить отсортированный список для отображения: сначала знакомые, потом новые.
    // Внутри групп сортировка по имени устройства.
    fun getDisplayDevices(): List<Pair<Boolean, BluetoothDevice>> {
        val knownAddresses = _knownDevices.value.map { it.address }.toSet()
        return _allDevices.value
            .map { device -> Pair(device.address in knownAddresses, device) }
            .sortedWith(compareBy<Pair<Boolean, BluetoothDevice>> { !it.first } // сначала true (знакомые)
                .thenBy { it.second.name ?: "" }) // затем по имени
    }

    fun connectToDevice(device: BluetoothDevice) {
        _connectionStatus.value = "Подключение..."
        bluetoothClient = BluetoothClient { socket ->
            currentSocket = socket
            try {
                inputStream = DataInputStream(socket.inputStream)
                Log.d("HeadUnitViewModel", "Подключено к серверу")
                _connectionStatus.value = "Подключено"
                saveKnownDevice(device)
                startListening()
            } catch (e: IOException) {
                e.printStackTrace()
                _connectionStatus.value = "Ошибка подключения"
            }
        }
        bluetoothClient?.connect(device.address)
    }

    private fun saveKnownDevice(device: BluetoothDevice) {
        val currentKnown = _knownDevices.value.toMutableList()
        val existing = currentKnown.find { it.address == device.address }
        if (existing == null) {
            val newDevice = BluetoothDeviceInfo(
                name = device.name ?: "Без имени",
                address = device.address,
                isAutoConnect = false,
                lastConnectedTimestamp = System.currentTimeMillis()
            )
            currentKnown.add(0, newDevice)
            _knownDevices.value = currentKnown
            dataStore.saveKnownDevices(currentKnown)
        } else {
            val updated = currentKnown.map {
                if (it.address == device.address) it.copy(lastConnectedTimestamp = System.currentTimeMillis()) else it
            }.sortedByDescending { it.lastConnectedTimestamp }
            _knownDevices.value = updated
            dataStore.saveKnownDevices(updated)
        }
    }

    fun setAutoConnect(deviceAddress: String, autoConnect: Boolean) {
        val updated = _knownDevices.value.map {
            if (it.address == deviceAddress) it.copy(isAutoConnect = autoConnect) else it
        }
        _knownDevices.value = updated
        dataStore.saveKnownDevices(updated)
    }

    private fun startListening() {
        receiveJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive && inputStream != null) {
                try {
                    val message = inputStream?.readUTF()
                    message?.let {
                        ttsManager.speak(it)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    fun disconnect() {
        receiveJob?.cancel()
        bluetoothClient?.disconnect()
        try {
            inputStream?.close()
            currentSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        bluetoothClient = null
        currentSocket = null
        inputStream = null
        _connectionStatus.value = "Отключено"
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
        ttsManager.shutdown()
    }
}