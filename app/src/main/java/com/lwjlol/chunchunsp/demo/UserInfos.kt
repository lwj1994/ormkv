package com.lwjlol.chunchunsp.demo

import com.lwjlol.ccsp.annotation.ColumnInfo
import com.lwjlol.ccsp.annotation.Entity

/**
 * @author luwenjie on 2019-08-11 20:16:58
 */
@Entity(getSpCode = "SPStores.sp")
data class UserInfos(
    @ColumnInfo(defValue = "david")
    val name: String,
    @ColumnInfo(defValue = "12")
    val age: Int,
    @ColumnInfo(defValue = "12312312")
    val id: Long,
    @ColumnInfo(defValue = "false")
    val isMan: Boolean
) {


    companion object {
        private const val TAG = "UserInfos"
    }
}