package com.lwjlol.chunchunsp.demo.sp

import android.content.Context
import com.lwjlol.chunchunsp.demo.App

object SpStore{
    val sp = App.context.getSharedPreferences("ccsp", Context.MODE_PRIVATE)
}





