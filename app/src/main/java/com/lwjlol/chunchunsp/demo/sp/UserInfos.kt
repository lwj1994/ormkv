package com.lwjlol.chunchunsp.demo.sp

import com.lwjlol.ccsp.annotation.ColumnInfo
import com.lwjlol.ccsp.annotation.Encrypt
import com.lwjlol.ccsp.annotation.Entity
import com.lwjlol.ccsp.annotation.Ignore

/**
 * @author luwenjie on 2019-08-11 20:16:58
 */
@Entity(name = "UserSp",getSpCode = "SpStore.sp")
@Encrypt(
    getEncryptCode = "SpUtil.encrypt",
    secret = "123wdskdhasdguyt1yu22eqwd"
)
data class UserInfos(
    @Ignore
    val skip: String,
    @ColumnInfo(defValue = "name")
    val name: String,
    @ColumnInfo(defValue = "12")
    val age: Int,
    @ColumnInfo(defValue = "12312312",clear = false)
    val id: Long,
    @ColumnInfo(defValue = "false")
    val isMan: Boolean,
    @ColumnInfo(defValue = "0F",clear = false)
    private val temperature: Float

) {


    companion object {
        private const val TAG = "UserInfos"
    }
}