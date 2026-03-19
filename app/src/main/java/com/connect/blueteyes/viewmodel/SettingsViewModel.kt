package com.connect.blueteyes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.connect.blueteyes.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = DataStoreManager(application)

    private val _isNotificationAccessEnabled = MutableStateFlow(dataStore.isNotificationAccessEnabled())
    val isNotificationAccessEnabled: StateFlow<Boolean> = _isNotificationAccessEnabled.asStateFlow()

    fun setNotificationAccessEnabled(enabled: Boolean) {
        _isNotificationAccessEnabled.value = enabled
        viewModelScope.launch {
            dataStore.setNotificationAccessEnabled(enabled)
        }
    }
}