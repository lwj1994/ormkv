package com.lwjlol.chunchunsp.demo

import com.lwjlol.ccsp.CcspEncrypt
import java.security.GeneralSecurityException

class MyCcspCrypt : CcspEncrypt() {

    @Throws(GeneralSecurityException::class)
    override fun encode(content: String, secret: String): String {
        return AESCrypt.encrypt(secret, content)
    }

    @Throws(GeneralSecurityException::class)
    override fun decode(content: String, secret: String): String {
        return AESCrypt.decrypt(secret, content)
    }
}