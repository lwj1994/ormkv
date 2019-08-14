
 [ ![Download](https://api.bintray.com/packages/wenchieh/maven/ccsp/images/download.svg) ](https://bintray.com/wenchieh/maven/bottombar/_latestVersion)
 ![](https://img.shields.io/badge/build-passing-green.svg)
 ![](https://img.shields.io/badge/license-MIT-orange.svg)
 
 ```gradle
implementation 'com.lwjlol.ccsp:ccsp:0.0.1'
kapt 'com.lwjlol.ccsp:compiler:0.0.1'
```
 
 ccsp 是一个结合内存和 sp 存储的工具. 将字段信息暂时存入内存中, 读取直接从内存中读取, 同时存入 sp.
 
 ## 使用方法
 你可以直接参考 sample 使用.  
 
 ccsp 将 sp 字段信息映射到一个实体 Entity 类.
 
 在一个实体类添加如下注解:
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
    val isMan: Boolean
)
```

#### @Entity

定义一个你要存储的实体类. ccsp 会解析这个实体类生成一个 `UserInfos_CCSP` 类. 你可以通过 `name` 指定生成文件的名字. 

```
@Entity(name = "UserInfoSP", getSpCode = "SPStores.sp")
```
`name`: 指定生成文件的名字  
 

`getSpCode`: 生成一个 sp 实例的代码.  

例: 在一个地方定义一个 `SPStores` 来存储所有的 sp 仓库.
```
object SPStores {
    val sp = App.context.getSharedPreferences("ccsp", Context.MODE_PRIVATE)
}
```
那么 `getSpCode = SPStores.sp`.  

这样设计可能有些粗糙. 但是 sp 的生成需要引入 context. 所以暂时这样妥协.

#### @ColumnInfo

对实体类中的字段信息进行补充.

`defValue`: 指定一个默认值. 

统一用 String 来指定默认值, 如给 Boolean 类型的字段指定默认值就是 `"false"`.

----
编译后 ccsp  会生成一个如下文件, 你就可以直接使用这个类来存储 sp 字段信息.

```
object UserInfos_CCSP {
  private val sp: SharedPreferences = SPStores.sp

  private var _name: String? = null

  var name: String
    get() {
      if (_name == null) {
         _name = sp.getString("name", "david")
      }
      return _name!!
    }
    set(value) {
      _name = value
      sp.edit().putString("name", value).apply()
    }

  private var _age: Int? = null

  var age: Int
    get() {
      if (_age == null) {
         _age = sp.getInt("age", 12)
      }
      return _age!!
    }
    set(value) {
      _age = value
      sp.edit().putInt("age", value).apply()
    }

  private var _id: Long? = null

  var id: Long
    get() {
      if (_id == null) {
         _id = sp.getLong("id", 12312312)
      }
      return _id!!
    }
    set(value) {
      _id = value
      sp.edit().putLong("id", value).apply()
    }

  private var _isMan: Boolean? = null

  var isMan: Boolean
    get() {
      if (_isMan == null) {
         _isMan = sp.getBoolean("isMan", false)
      }
      return _isMan!!
    }
    set(value) {
      _isMan = value
      sp.edit().putBoolean("isMan", value).apply()
    }

  fun clear() {
    name = "david" 
    age = 12 
    id = 12312312 
    isMan = false 
  }
}


```

更改 sp 信息:
```
UserInfos_CCSP.name = "mayun"
UserInfos_CCSP.age = 44
```

## 支持存储的字段类型

* String
* Int
* Long
* Boolean