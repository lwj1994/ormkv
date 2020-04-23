package com.lwjlol.ccsp.annotation

/**
 * @author luwenjie on 2020/4/23 11:14:26
 * @param secret 加密的密码
 * @param getEncryptCode 加密工具类的实例全路径类名 com.a.x.Util.encryptUtil
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Encrypt(val secret: String = "you must set yourself secret",val encryptClass:String = "you must set yourself encryptClass",val getEncryptCode:String = "you must set yourself getEncryptCode") {

    companion object {
        private const val TAG = "Encrypt"
    }
}