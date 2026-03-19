package com.connect.blueteyes.model

import android.os.Parcel
import android.os.Parcelable

data class HistoryRecord(
    val deviceName: String,
    val deviceAddress: String,
    val timestamp: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(deviceName)
        parcel.writeString(deviceAddress)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<HistoryRecord> {
        override fun createFromParcel(parcel: Parcel): HistoryRecord {
            return HistoryRecord(parcel)
        }

        override fun newArray(size: Int): Array<HistoryRecord?> {
            return arrayOfNulls(size)
        }
    }
}