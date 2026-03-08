package com.connect.blueteyes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.connect.blueteyes.viewmodel.PhoneViewModel

@Composable
fun PhoneScreen(viewModel: PhoneViewModel = viewModel()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Режим телефона (донор уведомлений)")
        Button(onClick = { viewModel.openNotificationAccess() }) {
            Text("Разрешить доступ к уведомлениям")
        }
        Button(onClick = { viewModel.startServer() }) {
            Text("Запустить Bluetooth-сервер")
        }
        Button(onClick = { viewModel.stopServer() }) {
            Text("Остановить сервер")
        }
    }
}