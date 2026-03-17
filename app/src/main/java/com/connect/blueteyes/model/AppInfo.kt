package com.connect.blueteyes.model

import android.graphics.drawable.Icon
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppInfo(
    val packageName: String,
    val appName: String,
    var isSelected: Boolean = false,
    val icon: Icon? = null // Опционально, для красоты
) : Parcelable