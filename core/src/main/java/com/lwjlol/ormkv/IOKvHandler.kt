package com.lwjlol.ormkv

/**
 * @author luwenjie on 2022/5/3 00:03:39
 */
abstract class IOKvHandler : OrmKvHandler {

    abstract fun isMainThread(): Boolean

    companion object {
        private const val TAG = "DefaultHanlder"
    }

    abstract fun putOnIoThread(key: String, value: Any)
    abstract fun getOnIoThread(key: String, default: Any): Any
    override fun put(key: String, value: Any) {
        if (!isMainThread()) {
            putOnIoThread(key, value)
        }
    }

    override fun get(key: String, default: Any): Any {
        if (isMainThread()) {
            return getOnIoThread(key, default)
        } else {
            throw IllegalStateException("must run on IO thread")
        }
    }
}