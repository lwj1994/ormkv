package com.lwjlol.ccsp

/**
 * @author luwenjie on 2020/4/23 11:17:51
 * SymmetricEncrypt like Aes Encrypt，extend this class implement your Symmetric Encrypt.
 */
abstract class SymmetricEncrypt {
    abstract fun encode(content: String, secret: String): String

    abstract fun decode(content: String, secret: String): String
}
