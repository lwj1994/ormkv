
 [ ![Download](https://api.bintray.com/packages/wenchieh/maven/ccsp/images/download.svg) ](https://bintray.com/wenchieh/maven/bottombar/_latestVersion)
 ![](https://img.shields.io/badge/build-passing-green.svg)
 ![](https://img.shields.io/badge/license-MIT-orange.svg)

 ```gradle
implementation 'com.lwjlol.ccsp:ccsp:0.0.2'
kapt 'com.lwjlol.ccsp:compiler:0.0.2'
 ```

 ccsp 是一个将实体 Bean 类映射成 Android SharedPreference 数据的工具。

* 类似将数据库映射到实体类的编写方式，方便管理
* 支持对 String 值加密

## 使用方法  

你可以直接参考 sample 使用.

ccsp 将 一个实体 Entity 类 生成 sp 数据信息.


例如，和一些 sql 数据库（比如 Room）的注解类似，在一个 `UserInfos` Bean 类添加如下注解:
`@Entity` 标识这个类会是一个 sp 的实体类，`@getSpCode` 是获取 `SharedPreference` 实例的代码(要带上包名)，`@ColumnInfo`可以自定义字段的默认值和一些其他信息。
```
@Entity(name = "UserInfoSP", getSpCode = "com.a.b.SPStores.sp")
data class UserInfos(
    @ColumnInfo(defValue = "david")
    val name: String,
    @ColumnInfo(defValue = "12")
    val age: Int,
    @ColumnInfo(defValue = "12312312")
    val id: Long,
    @ColumnInfo(defValue = "false")
    val isMan: Boolean
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

支持存储的字段类型：

* String
* Int
* Long
* Boolean
* Float

## 注解的含义

### @Entity

定义一个你要存储的实体类. ccsp 会解析这个实体类生成一个 `UserInfos_CCSP` 类. 你可以通过 `name` 指定生成文件的名字.

```
@Entity(name = "UserInfoSP", getSpCode = "SPStores.sp")
```

#### `name`: 指定生成文件的名字

#### `getSpCode`: 生成一个 sp 实例的代码.__注意:  `getSpCode` 必须包含包名路径，如 `com.a.b.SPStores.sp`__

例: 在一个地方定义一个 `SPStores` 来存储所有的 sp 仓库

```
object SPStores {
    val sp = App.context.getSharedPreferences("ccsp", Context.MODE_PRIVATE)
}
```


那么 `getSpCode = SPStores.sp`.

这样设计可能有些粗糙. 但是 sp 的生成需要引入 context. 所以暂时这样妥协.


### @ColumnInfo

对实体类中的字段信息进行补充. 不注释 @ColumnInfo 时，默认为基本类型的初始值。

#### `defValue`.
为字段数据指定一个默认值，统一用 String 来指定默认值, 如给 Boolean 类型的字段指定默认值就是 `"false"`.

#### `clear`

清空 sp 所有信息时，是否加入。false : clear() 方法不会清空此字段数据，true: clear() 方法会清空此字段数据，默认为 true


### @Skip
跳过某个属性，如果不想让某个字段参与 sp 数据的生成，可以加上这个。
编译后 ccsp  会生成一个如下文件, 你就可以直接使用这个类来存储 sp 字段信息.



### @Encrypt

定义值的字符串的加密信息

#### `getEncryptCode`

你的 aes 加密工具类__实例__的路径，aes加密工具类需继承 `CcspEncrypt`，需要包含包名，如 `com.a.b.Until.encrypt`
`secret`: 加密的密码

```
package com.a.b

// 你自己的加密类，实现 aes 加密逻辑
class MyCcspEncrypt:CcspEncrypt{

}

Class Util{
  val encrypt = MyCcspEncrypt()
}
```


----




