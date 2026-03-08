package com.connect.blueteyes.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.*
import java.io.IOException
import java.util.UUID

class BluetoothClient(private val onConnected: (BluetoothSocket) -> Unit) {
    private var connectJob: Job? = null

    fun connect(deviceAddress: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
        val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        connectJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                socket.connect()
                onConnected(socket)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun disconnect() {
        connectJob?.cancel()
    }
}