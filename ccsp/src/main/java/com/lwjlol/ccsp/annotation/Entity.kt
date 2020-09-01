package com.lwjlol.ccsp.annotation


/**
 * @param getSpCode 返回一个 sp 实例的代码，尽量和 entity 的包名一样，如 SpStore.sp1，如果不一样需要带上包名 com.a.b.SpStore.sp1（release 需要 keep 住混淆）
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