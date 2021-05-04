package com.lwjlol.ccsp.annotation

/**
 * @author luwenjie on 2020/4/23 11:14:26
 * the info of SymmetricEncryption
 * @param secret
 * @param getEncryptCode the code for Encrypt.
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SymmetricEncryptionInfo(val secret: String = "you must set yourself secret", val getEncryptCode: String = "you must set yourself getEncryptCode") {
    companion object {
        private const val TAG = "Encrypt"
    }
}
