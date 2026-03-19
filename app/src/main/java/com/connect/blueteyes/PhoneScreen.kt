package com.connect.blueteyes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.connect.blueteyes.viewmodel.PhoneViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhoneScreen(viewModel: PhoneViewModel = viewModel()) {
    val isServerRunning by viewModel.isServerRunning.collectAsStateWithLifecycle()
    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val history by viewModel.connectionHistory.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Режим телефона (донор уведомлений)")

        Spacer(modifier = Modifier.height(8.dp))

        Text(if (isServerRunning) "✅ Сервер запущен" else "⛔ Сервер остановлен")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.openNotificationAccess() }) {
            Text("Разрешить доступ к уведомлениям")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.startServer() }) {
            Text("Запустить Bluetooth-сервер")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.stopServer() }) {
            Text("Остановить сервер")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Приложения для отслеживания:")
        LazyColumn(modifier = Modifier.height(200.dp)) {
            items(allApps) { app ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = app.isSelected,
                        onCheckedChange = { viewModel.updateAppSelection(app, it) }
                    )
                    Text(app.appName, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("История подключений:")
        LazyColumn {
            items(history) { record ->
                val date = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(record.timestamp))
                Text("${record.deviceName} - $date")
                HorizontalDivider()
            }
        }
    }
}