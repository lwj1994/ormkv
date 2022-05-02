package com.lwjlol.ormkv.annotation

/**
 * @param defaultValue
 * @param name
 * @param enableReset reset to [defaultValue]
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ColumnInfo(
    val defaultValue: String = "",
    val name: String = "",
    val enableReset: Boolean = true,
)
