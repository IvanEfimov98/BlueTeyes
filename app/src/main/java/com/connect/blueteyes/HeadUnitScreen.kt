package com.connect.blueteyes

import androidx.compose.runtime.livedata.observeAsState
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.connect.blueteyes.viewmodel.HeadUnitViewModel

@Composable
fun HeadUnitScreen(viewModel: HeadUnitViewModel = viewModel()) {
    val devices by viewModel.pairedDevices.observeAsState(emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Режим магнитолы")
        Button(onClick = { viewModel.scanDevices() }) {
            Text("Поиск сопряжённых устройств")
        }
        LazyColumn {
            items(devices) { device ->
                Button(onClick = { viewModel.connectToDevice(device.address) }) {
                    Text(device.name ?: "Без имени")
                }
            }
        }
        Button(onClick = { viewModel.disconnect() }) {
            Text("Отключиться")
        }
    }
}