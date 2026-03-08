package com.connect.blueteyes.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.*
import java.io.IOException
import java.util.UUID

class BluetoothServer(private val onConnected: (BluetoothSocket) -> Unit) {
    private var serverSocket: BluetoothServerSocket? = null
    private var isRunning = false
    private var acceptJob: Job? = null

    fun start() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // стандартный UUID
        val name = "BlueTeyesServer"

        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, MY_UUID)
            isRunning = true
            acceptJob = CoroutineScope(Dispatchers.IO).launch {
                while (isRunning) {
                    val socket = try {
                        serverSocket?.accept()
                    } catch (e: IOException) {
                        null
                    }
                    socket?.let {
                        onConnected(it)
                        // Здесь можно запустить поток для приёма данных
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        isRunning = false
        acceptJob?.cancel()
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}