package com.lwjlol.chunchunsp.demo.sp

import android.content.Context
import com.lwjlol.chunchunsp.demo.App

object SpUtil{
    val encrypt by lazy {
        MyCcspCrypt()
    }
}