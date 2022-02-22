package com.lwjlol.ormkv.demo

import com.lwjlol.ormkv.annotation.ColumnInfo
import com.lwjlol.ormkv.annotation.Entity
import com.lwjlol.ormkv.annotation.Ignore

/**
 * @author luwenjie on 2019-08-11 20:16:58
 */
@Entity(className = "com.test.UserSp", handlerCodeReference = "com.lwjlol.ormkv.demo.KvStore.sp")
data class UserModel(
    @Ignore
    val skip: String,
    @ColumnInfo(defValue = "qweqweqe")
    val name: String,
    @Ignore
    @ColumnInfo(defValue = "12")
    val age: Int,
    @ColumnInfo(defValue = "12312312", enableReset = false)
    val id: Long,
    @ColumnInfo(defValue = "false")
    val isMan: Boolean,
    @ColumnInfo(defValue = "0F", enableReset = false)
    val temperature: Float

) {

    companion object {
        private const val TAG = "UserInfos"
    }
}

@Entity
class TestModel(a: String, b: String)