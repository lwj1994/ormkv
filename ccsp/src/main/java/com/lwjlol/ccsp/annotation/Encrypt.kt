package com.lwjlol.ccsp.annotation

/**
 * @author luwenjie on 2020/4/23 11:14:26
 * @param secret 加密的密码
 * @param getEncryptCode 加密工具类的实例，尽量和 entity 的包名一样，如 Util.encryptUtil，如果不一样需要带上包名 com.a.x.Util.encryptUtil（release 需要 keep 住混淆）
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Encrypt(val secret: String = "you must set yourself secret",val getEncryptCode:String = "you must set yourself getEncryptCode") {

    companion object {
        private const val TAG = "Encrypt"
    }
}