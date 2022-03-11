package com.lwjlol.ormkv.demo

import com.lwjlol.ormkv.annotation.ColumnInfo
import com.lwjlol.ormkv.annotation.Entity
import com.lwjlol.ormkv.annotation.Ignore

/**
 * @author luwenjie on 2019-08-11 20:16:58
 */
@Entity(
    className = "User",
    handlerCodeReference = "com.lwjlol.ormkv.demo.KvStore.sp",
    prefixKeyWithClassName = true
)
data class UserModel(
    @ColumnInfo(name = "SECNAME_XXD", defaultValue = Constants.PWD)
    val secname: String,
    @Ignore
//    val skip: String,
    @ColumnInfo(defaultValue = "qweqweqe")
    val name: String,
    @Ignore
    @ColumnInfo(defaultValue = "12")
    val age: Int,
    @ColumnInfo(defaultValue = "12312312", enableReset = false)
    val id: Long,
    @ColumnInfo(defaultValue = "false")
    val isMan: Boolean,
    @ColumnInfo(defaultValue = "1231", enableReset = false)
    val temperature: Float

) {

    companion object {
        private const val TAG = "UserInfos"
    }
}

@Entity
class TestModel(a: String, b: String)