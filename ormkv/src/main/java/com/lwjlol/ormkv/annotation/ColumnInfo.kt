package com.lwjlol.ormkv.annotation

/**
 * @param defValue 默认值
 * @param enableReset 允许恢复默认值
 * @param handlerCodeReference
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ColumnInfo(
  val defValue: String = "",
  val enableReset: Boolean = true
)
