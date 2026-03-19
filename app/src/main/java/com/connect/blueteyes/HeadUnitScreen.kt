package com.connect.blueteyes

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.connect.blueteyes.viewmodel.HeadUnitViewModel

@Composable
fun HeadUnitScreen(viewModel: HeadUnitViewModel = viewModel()) {
    val allDevices by viewModel.allDevices.collectAsStateWithLifecycle()
    val knownDevices by viewModel.knownDevices.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    // Получаем отсортированный список для отображения
    val displayList = remember(allDevices, knownDevices) {
        viewModel.getDisplayDevices()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Режим магнитолы")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Статус: $connectionStatus")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.scanDevices() }) {
            Text("Поиск сопряжённых устройств")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning) {
            CircularProgressIndicator()
        } else {
            if (displayList.isEmpty()) {
                Text("Нет сопряжённых устройств")
            } else {
                LazyColumn {
                    // Разделяем на группы: сначала известные, потом новые
                    val knownGroup = displayList.filter { it.first }
                    val newGroup = displayList.filter { !it.first }

                    if (knownGroup.isNotEmpty()) {
                        item {
                            Text("Ранее подключались", modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(knownGroup) { (_, device) ->
                            DeviceItem(
                                device = device,
                                isKnown = true,
                                autoConnect = knownDevices.find { it.address == device.address }?.isAutoConnect ?: false,
                                onAutoConnectChange = { viewModel.setAutoConnect(device.address, it) },
                                onConnect = { viewModel.connectToDevice(device) }
                            )
                        }
                    }

                    if (newGroup.isNotEmpty()) {
                        item {
                            Text("Новые устройства", modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(newGroup) { (_, device) ->
                            DeviceItem(
                                device = device,
                                isKnown = false,
                                autoConnect = false,
                                onAutoConnectChange = {},
                                onConnect = { viewModel.connectToDevice(device) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.disconnect() }) {
            Text("Отключиться")
        }
    }
}

@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isKnown: Boolean,
    autoConnect: Boolean,
    onAutoConnectChange: (Boolean) -> Unit,
    onConnect: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Button(onClick = onConnect) {
            Text(device.name ?: "Без имени")
        }
        if (isKnown) {
            // Добавляем чекбокс для автоподключения
            Switch(
                checked = autoConnect,
                onCheckedChange = onAutoConnectChange,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        HorizontalDivider()
    }
}