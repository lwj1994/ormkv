package com.lwjlol.ormkv.mmkv

import android.os.Parcelable
import com.lwjlol.ormkv.OrmKvHandler
import com.tencent.mmkv.MMKV

open class MmkvHandler(val mmkv: MMKV) : OrmKvHandler {

  override fun put(key: String, value: Any) {
    when (value) {
      is Boolean -> {
        mmkv.encode(key, value)
      }
      is Int -> {
        mmkv.encode(key, value)
      }
      is Float -> {
        mmkv.encode(key, value)
      }
      is Long -> {
        mmkv.encode(key, value)
      }
      is String -> {
        mmkv.encode(key, value)
      }
      is Set<*> -> {
        @Suppress("UNCHECKED_CAST")
        mmkv.encode(key, value as Set<String>)
      }
      is Parcelable -> {
        mmkv.encode(key, value)
      }
      is ByteArray -> {
        mmkv.encode(key, value)
      }
      else -> throw IllegalArgumentException("unSupport ${value::class.qualifiedName}")
    }
  }

  override fun get(key: String, default: Any): Any {
    when (default) {
      is Boolean -> {
        return mmkv.decodeBool(key, default)
      }
      is Int -> {
        return mmkv.decodeInt(key, default)
      }
      is Float -> {
        return mmkv.decodeFloat(key, default)
      }
      is Long -> {
        return mmkv.decodeLong(key, default)
      }
      is String -> {
        return mmkv.decodeString(key, default) ?: NullPointerException("")
      }
      is Set<*> -> {
        @Suppress("UNCHECKED_CAST")
        return mmkv.decodeStringSet(key, default as Set<String>) ?: NullPointerException("")
      }
      is ByteArray -> {
        return mmkv.decodeBytes(key, default) ?: NullPointerException("")
      }
      else -> throw IllegalArgumentException("unSupport ${default::class.qualifiedName}")
    }
  }

}
