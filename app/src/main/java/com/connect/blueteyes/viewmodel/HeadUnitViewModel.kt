package com.connect.blueteyes.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.connect.blueteyes.service.BluetoothClient
import com.connect.blueteyes.tts.TTSManager
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.IOException

class HeadUnitViewModel(application: Application) : AndroidViewModel(application) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothClient: BluetoothClient? = null
    private var currentSocket: BluetoothSocket? = null
    private var inputStream: DataInputStream? = null
    private val ttsManager = TTSManager(application)

    private val _pairedDevices = MutableLiveData<List<BluetoothDevice>>()
    val pairedDevices: LiveData<List<BluetoothDevice>> = _pairedDevices

    private var receiveJob: Job? = null

    fun scanDevices() {
        val devices = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        _pairedDevices.postValue(devices)
    }

    fun connectToDevice(deviceAddress: String) {
        bluetoothClient = BluetoothClient { socket ->
            currentSocket = socket
            try {
                inputStream = DataInputStream(socket.inputStream)
                Log.d("HeadUnitViewModel", "Подключено к серверу")
                startListening()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        bluetoothClient?.connect(deviceAddress)
    }

    private fun startListening() {
        receiveJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && inputStream != null) {
                try {
                    val message = inputStream?.readUTF()
                    message?.let {
                        // Озвучиваем полученное сообщение
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
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
        ttsManager.shutdown()
    }
}