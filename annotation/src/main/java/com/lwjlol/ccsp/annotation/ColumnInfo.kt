package com.lwjlol.ccsp.annotation


/**
 * @param defValue 默认值
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ColumnInfo(val defValue: String = "") {


    companion object {
        private const val TAG = "ColumnInfo"
    }
}