package com.lwjlol.ccsp

/**
 * @author luwenjie on 2020/4/23 11:17:51
 */
abstract class CcspEncrypt {

    abstract fun encode(content: String, secret: String): String

    abstract fun decode(content: String, secret: String): String

    companion object {
        private const val TAG = "CcspEncrypt"
    }
}