package com.lwjlol.chunchunsp.demo

import android.content.Context

/**
 * @author luwenjie on 2019-08-13 23:33:38
 */
object SPStores {
    val sp = App.context.getSharedPreferences("ccsp", Context.MODE_PRIVATE)
}