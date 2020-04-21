package com.lwjlol.chunchunsp.demo

import com.lwjlol.ccsp.annotation.ColumnInfo
import com.lwjlol.ccsp.annotation.Entity
@Entity(name = "UserRegistry", getSpCode = "SPStores.sp")
data class IHUser(
  @ColumnInfo(defValue = "") val username: String,
  @ColumnInfo(defValue = "") val password: String,
  @ColumnInfo(defValue = "") val email: String = "",
  @ColumnInfo(defValue = "") val objectId: String = "",
  @ColumnInfo(defValue = "") val loginId: String = "",
  @ColumnInfo(defValue = "0") val loginState: Int = 0,
  @ColumnInfo(defValue = "") val deviceModel: String,
  @ColumnInfo(defValue = "") val emailVerified: Boolean = false,
  @ColumnInfo(defValue = "") val avatar: String = "",
  @ColumnInfo(defValue = "0") val primeType: Int,
  @ColumnInfo(defValue = "") val expirationDate: String = "",
  @ColumnInfo(defValue = "") val createdAt: String = "",
  @ColumnInfo(defValue = "") val sessionToken: String = "",
  @ColumnInfo(defValue = "0") val pictureCount: Long = 0)