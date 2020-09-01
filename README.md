
 [ ![Download](https://api.bintray.com/packages/wenchieh/maven/ccsp/images/download.svg) ](https://bintray.com/wenchieh/maven/bottombar/_latestVersion)
 ![](https://img.shields.io/badge/build-passing-green.svg)
 ![](https://img.shields.io/badge/license-MIT-orange.svg)

 ```gradle
implementation 'com.lwjlol.ccsp:ccsp:0.2.0'
kapt 'com.lwjlol.ccsp:compiler:0.2.0'
 ```

 ccsp 是一个将实体 Bean 类映射成 Android SharedPreference 数据的工具。

* 类似将数据库映射到实体类的编写方式，方便管理
* 支持对 String 值加密


支持存储的字段类型：

* String
* Int
* Long
* Boolean
* Float

## 使用方法

你可以直接参考 sample 使用.

ccsp 将 一个实体 Entity 类 生成 sp 数据信息.


例如，和一些 sql 数据库框架（比如 Room）的注解类似，在一个 `UserInfos` Bean 类添加如下注解:
`@Entity` 标识这个类会是一个 sp 的实体类，`@getSpCode` 是获取 `SharedPreference` 实例的代码，`@ColumnInfo`可以自定义字段的默认值和一些其他信息。
```
@Entity(name = "UserInfoSP", getSpCode = "SPStores.sp")
data class UserInfos(
    @ColumnInfo(defValue = "david")
    val name: String,
    @ColumnInfo(defValue = "12")
    val age: Int,
    @ColumnInfo(defValue = "12312312")
    val id: Long,
    @ColumnInfo(defValue = "false")
    val isMan: Boolean,
    @Ignore
    val ignore: Boolean
)
```
然后编译后会自动在 `UserInfos` 的同级目录生成一个 `UserInfoSP` 类，可以直接使用此类进行如下操作：
```
// 直接给 sp 的字段赋值
UserInfoSP.name = "qweq"
UserInfoSP.age = 123
UserInfoSP.id = 1231321
UserInfoSP.isMan = false

// 直接读取字段信息
println(UserInfoSP.name)


// 清除所有sp信息，恢复为默认值
UserInfoSP.clear()
```


## 注解的含义

### @Entity

定义一个你要存储的实体类. ccsp 会解析这个实体类生成一个 `UserInfos_CCSP` 类. 你可以通过 `name` 指定生成文件的名字.

```
@Entity(name = "UserInfoSP", getSpCode = "SPStores.sp")
```

*  `name`
    指定生成文件的名字

* `getSpCode`
    生成一个 sp 实例的代码， SpStore 尽量和 entity 的包名一样，如 SpStore.sp1，如果不一样需要带上包名 com.a.b.SpStore.sp1（release 需要 keep 住混淆）

例: 在一个地方定义一个 `SPStores` 来存储所有的 sp 仓库

```
object SPStores {
    val sp = App.context.getSharedPreferences("ccsp", Context.MODE_PRIVATE)
}
```


那么 `getSpCode = SPStores.sp`.

这里的 SpStore 和 UserInfo 都是在一个包名下的，所以不需要声明包名。建议都放在一个包名下统一管理。

```
com.lwjlol.ccsp
    - demo
        - sp
            - SpStore
            - UserInfos

```

### @ColumnInfo

对实体类中的字段信息进行补充. 不注释 @ColumnInfo 时，默认为基本类型的初始值。

* `defValue`
为字段数据指定一个默认值，统一用 String 来指定默认值, 如给 Boolean 类型的字段指定默认值就是 `"false"`.

* `clear`
是否支持清空该字段，将该字段还原成默认值。
如果设置 `true` 会在 `clear()` 方法自动将字段值重置。默认为 `true`
```
  fun clear() {
    name = """name"""
    age = 12
    isMan = false
  }
```



### @Ignore
跳过某个属性，如果不想让某个字段参与 sp 数据的生成，可以加上这个。



### @Encrypt
```
@Encrypt(
    getEncryptCode = "SpUtil.encrypt",
    secret = "123wdskdhasdguyt1yu22eqwd"
)
```
定义值的字符串的加密信息，使用 AES 加密，需继承 `CcspEncrypt` 类实现您的自定义加密逻辑

* `getEncryptCode`

你的 aes 加密类实例的路径，aes 加密工具类需继承 `CcspEncrypt`，和上述 Entity#getSpCode 的用法一样，不在同一个包名下需要加上包名。
* `secret`: 加密的密码

```
package com.a.b

// 你自己的加密类，实现 aes 加密逻辑
class MyCcspEncrypt:CcspEncrypt{

}

Class SpUtil{
  val encrypt = MyCcspEncrypt()
}
```
