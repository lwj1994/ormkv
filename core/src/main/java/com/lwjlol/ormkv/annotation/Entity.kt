package com.lwjlol.ormkv.annotation

/**
 * @param handler [com.lwjlol.ormkv.OrmKvHandler]
 * @param className a name , can include package. name or  com.lwjl.name is also ok.
 * @param prefixKeyWithClassName if true, will add className as key's prefix
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Entity(
    val className: String = "",
    val handler: String = "",
    val prefixKeyWithClassName: Boolean = false,
)
