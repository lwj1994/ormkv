 [![](https://jitpack.io/v/lwj1994/ormkv.svg)](https://jitpack.io/#lwj1994/ormkv)
 ![](https://img.shields.io/badge/build-passing-green.svg)
 ![](https://img.shields.io/badge/license-MIT-orange.svg)

```gradle
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io'}
    }
}
```

```gradle
	dependencies {
	   implementation "com.github.lwj1994.ormkv:core:${latestVersion}""
	   // sharedPreferences
	   implementation "com.github.lwj1994.ormkv:sharedPreferences:${latestVersion}"
	   // option :if you use mmkv
	   implementation "com.github.lwj1994.ormkv:mmkv:${latestVersion}"
	   kapt "com.github.lwj1994.ormkv:compiler:${latestVersion}"
	}
```

ormkv 是一个根据 Bean 类的配置自动生成__键值对数据工具类__的助手，类似 Room 将数据库映射到实体类的编写方式，方便管理。

## 使用方法

* 使用 `@Entity` 注解标记一个实体类作为生成 key-value 存储的模型
* build 生成相应的 `key-value` 工具类
* 使用工具类 `put/get` 数据 

可以直接参考 sample 使用.


和一些 sql 数据库框架（比如 Room）的注解类似，在一个 `UserInfos` Bean 类添加如下注解:
`@Entity` 标识这个类会是一个 sp 的实体类，`@ColumnInfo`可以自定义字段的默认值和一些其他信息。

`@handlerCodeReference` 表示处理存取值的 handler 类的实例引用代码。

```kotlin
@Entity(name = "com.test.UserInfoSP", // 指定生成的类名
        handlerCodeReference = "com.test.SPStoreUtil.getSp()"
)
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

然后编译后会自动在 `UserInfos` 的生成一个 `com.test.UserInfoSP` 类，可以直接使用此类进行如下操作：

```kotlin
// 直接给 sp 的字段赋值
UserInfoSP.name = "qweq"
UserInfoSP.age = 123
UserInfoSP.id = 1231321
UserInfoSP.isMan = false

// 直接读取字段信息
println(UserInfoSP.name)


// 清除所有sp信息，恢复为默认值
UserInfoSP.reset()
```
