package com.lwjlol.ormkv.demo

import android.content.Context
import com.lwjlol.ormkv.mmkv.MmkvHandler
import com.lwjlol.ormkv.sp.SharedPreferencesHandler

object KvStore {
    val sp =
        SharedPreferencesHandler(App.context.getSharedPreferences("ccsp", Context.MODE_PRIVATE))
    val mmkv = MmkvHandler(com.tencent.mmkv.MMKV.defaultMMKV()!!)
}





