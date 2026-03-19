package com.connect.blueteyes.model

import android.os.Parcel
import android.os.Parcelable
import android.graphics.drawable.Icon

data class AppInfo(
    val packageName: String,
    val appName: String,
    var isSelected: Boolean = false,
    val icon: Icon? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(Icon::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(appName)
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeParcelable(icon, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AppInfo> {
        override fun createFromParcel(parcel: Parcel): AppInfo {
            return AppInfo(parcel)
        }

        override fun newArray(size: Int): Array<AppInfo?> {
            return arrayOfNulls(size)
        }
    }
}