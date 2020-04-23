package com.lwjlol.ccsp.annotation


/**
 * @param getSpCode 返回一个 sp 实例的代码，需要带上包名
 * @param name 别名
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Entity(
    val getSpCode: String = "\"you must Provide a code to get a SharedPreferences instance\"",
    val name: String = ""
) {


    companion object {
        private const val TAG = "SPEntity"
    }
}