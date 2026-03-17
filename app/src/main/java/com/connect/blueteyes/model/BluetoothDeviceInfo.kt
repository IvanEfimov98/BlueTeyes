package com.connect.blueteyes.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    var isAutoConnect: Boolean = false,
    var lastConnectedTimestamp: Long = 0L // Для сортировки по свежести
) : Parcelable