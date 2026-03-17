package com.connect.blueteyes.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryRecord(
    val deviceName: String,
    val deviceAddress: String,
    val timestamp: Long
) : Parcelable