package com.lwjlol.chunchunsp.demo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * @author luwenjie on 2019-08-13 23:35:43
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        lateinit var context: Context
        private const val TAG = "App"
    }
}