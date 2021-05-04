package com.lwjlol.chunchunsp.demo.sp

import com.lwjlol.ccsp.SymmetricEncrypt
import com.lwjlol.chunchunsp.demo.AESCrypt
import java.security.GeneralSecurityException

class MyCcspCrypt : SymmetricEncrypt() {
    @Throws(GeneralSecurityException::class)
    override fun encode(content: String, secret: String): String {
        return AESCrypt.encrypt(secret, content)
    }

    @Throws(GeneralSecurityException::class)
    override fun decode(content: String, secret: String): String {
        return AESCrypt.decrypt(secret, content)
    }
}
