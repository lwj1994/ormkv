package com.lwjlol.ormkv.annotation

/**
 * @param handlerCodeReference com.a.b.SpStore.sp
 * @param className a name , can include package. name or  com.lwjl.name is also ok.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Entity(
  val className: String = "",
  val handlerCodeReference: String = ""
)
