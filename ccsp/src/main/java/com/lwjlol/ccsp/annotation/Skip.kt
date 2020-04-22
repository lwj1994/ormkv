package com.lwjlol.ccsp.annotation

/**
 * @author luwenjie on 2020/4/22 21:57:40
 * 跳过某个字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Skip {

    companion object {
        private const val TAG = "Skip"
    }
}