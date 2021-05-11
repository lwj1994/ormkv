package com.lwjlol.ormkv.sp

import android.content.SharedPreferences
import com.lwjlol.ormkv.OrmKvHandler

open class SharedPreferencesHandler(val sp: SharedPreferences) : OrmKvHandler {
  override fun put(key: String, value: Any) {
    sp.edit().apply {
      when (value) {
        is Boolean -> {
          putBoolean(key, value)
        }
        is Int -> {
          putInt(key, value)
        }
        is Float -> {
          putFloat(key, value)
        }
        is Long -> {
          putLong(key, value)
        }
        is String -> {
          putString(key, value)
        }
        is Set<*> -> {
          @Suppress("UNCHECKED_CAST")
          putStringSet(key, value as Set<String>)
        }
        else -> throw IllegalArgumentException("unSupport ${value::class.qualifiedName}")
      }
    }.apply()
  }

  override fun get(key: String, default: Any): Any {
    if (default is Boolean) {
      return sp.getBoolean(key, default)
    }
    if (default is Int) {
      return sp.getInt(key, default)
    }
    if (default is Float) {
      return sp.getFloat(key, default)
    }

    if (default is Long) {
      return sp.getLong(key, default)
    }
    if (default is String) {
      return sp.getString(key, default) ?: ""
    }

    if (default is Set<*>) {
      @Suppress("UNCHECKED_CAST")
      return sp.getStringSet(key, default as Set<String>) ?: throw NullPointerException("")
    }

    throw IllegalAccessError("")
  }
}
