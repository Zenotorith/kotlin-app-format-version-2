package com.example.kotlinnativevincent.models

import android.os.Parcel
import android.os.Parcelable


data class ShowListTrip(
    val tripName : String ?= null,
    val destination : String ?= null,
    val date : String ?= null,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tripName)
        parcel.writeString(destination)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowListTrip> {
        override fun createFromParcel(parcel: Parcel): ShowListTrip {
            return ShowListTrip(parcel)
        }

        override fun newArray(size: Int): Array<ShowListTrip?> {
            return arrayOfNulls(size)
        }
    }
}
