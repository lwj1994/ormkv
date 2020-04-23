package com.lwjlol.chunchunsp.demo

import com.lwjlol.ccsp.annotation.ColumnInfo
import com.lwjlol.ccsp.annotation.Encrypt
import com.lwjlol.ccsp.annotation.Entity
import com.lwjlol.ccsp.annotation.Skip

/**
 * @author luwenjie on 2019-08-11 20:16:58
 */
@Entity(getSpCode = "com.lwjlol.chunchunsp.demo.MainActivity.sp")
@Encrypt(
    getEncryptCode = "com.lwjlol.chunchunsp.demo.MainActivity.encrypt",
    secret = "123wdskdhasdguyt1yu22eqwd"
)
data class UserInfos(
    @Skip
    val skip: String,
    @ColumnInfo(defValue = "david")
    val name: String,
    @ColumnInfo(defValue = "12")
    val age: Int,
    @ColumnInfo(defValue = "12312312")
    val id: Long,
    @ColumnInfo(defValue = "false")
    val isMan: Boolean,
    @ColumnInfo(defValue = "0F")
    private val temperature: Float

) {


    companion object {
        private const val TAG = "UserInfos"
    }
}