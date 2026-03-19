package com.connect.blueteyes.model

import android.os.Parcel
import android.os.Parcelable

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    var isAutoConnect: Boolean = false,
    var lastConnectedTimestamp: Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeByte(if (isAutoConnect) 1 else 0)
        parcel.writeLong(lastConnectedTimestamp)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BluetoothDeviceInfo> {
        override fun createFromParcel(parcel: Parcel): BluetoothDeviceInfo {
            return BluetoothDeviceInfo(parcel)
        }

        override fun newArray(size: Int): Array<BluetoothDeviceInfo?> {
            return arrayOfNulls(size)
        }
    }
}