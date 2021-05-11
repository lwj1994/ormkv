package com.lwjlol.ormkv

interface OrmKvHandler {
  fun put(key: String, value: Any)

  fun get(key: String, default: Any): Any
}
