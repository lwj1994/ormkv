package com.lwjlol.ormkv.demo

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.lwjlol.ormkv.annotation.ColumnInfo
import com.lwjlol.ormkv.annotation.Entity

@Entity(className = "com.test.ConfigMmkv", handlerCodeReference = "com.lwjlol.ormkv.demo.KvStore.mmkv")
data class ConfigModel(
  val value1: String,
  val value2: String,
  val value3: ByteArray,
)

class A(val a: String) : Parcelable {
  constructor(parcel: Parcel) : this(parcel.readString() ?: "") {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(a)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Creator<A> {
    override fun createFromParcel(parcel: Parcel): A {
      return A(parcel)
    }

    override fun newArray(size: Int): Array<A?> {
      return arrayOfNulls(size)
    }
  }
}

