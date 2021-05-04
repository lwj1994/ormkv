package com.lwjlol.ccsp.annotation


/**
 * @param getSpCode com.a.b.SpStore.sp
 * @param name a name , can include package. name or  com.lwjl.name is also ok.
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
