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

## KSP

1.2.0 已支持 ksp 使用方法： KSP Generated Sources in IDE Note, that as of the current KSP version generated
java sources are detected by the IDE but NOT generated kotlin sources. This means that generated
epoxy kotlin extensions will not automatically be resolved in the IDE. You must manually configure
your source sets to include ksp generated folders.

You can add this to your root build.gradle file to work around this

```gradle
subprojects { project ->
    afterEvaluate {
        if (project.hasProperty("android")) {
            android {
                if (it instanceof com.android.build.gradle.LibraryExtension) {
                    libraryVariants.all { variant ->
                        def outputFolder = new File("build/generated/ksp/${variant.name}/kotlin")
                        variant.addJavaSourceFoldersToModel(outputFolder)
                        android.sourceSets.getAt(variant.name).java {
                            srcDir(outputFolder)
                        }
                    }
                } else if (it instanceof com.android.build.gradle.AppExtension) {
                    applicationVariants.all { variant ->
                        def outputFolder = new File("build/generated/ksp/${variant.name}/kotlin")
                        variant.addJavaSourceFoldersToModel(outputFolder)
                        android.sourceSets.getAt(variant.name).java {
                            srcDir(outputFolder)
                        }
                    }
                }
            }
        }
    }
}
```

Of if you use kotlin build files you can apply it like this to a project.

```kotlin
/**
 * Return the Android variants for this module, or error if this is not a module with a known Android plugin.
 */
fun Project.requireAndroidVariants(): DomainObjectSet<out com.android.build.gradle.api.BaseVariant> {
 return androidVariants() ?: error("no known android extension found for ${project.name}")
}

/**
 * Return the Android variants for this module, or null if this is not a module with a known Android plugin.
 */
fun Project.androidVariants(): DomainObjectSet<out com.android.build.gradle.api.BaseVariant>? {
 return when (val androidExtension = this.extensions.findByName("android")) {
  is com.android.build.gradle.LibraryExtension -> {
   androidExtension.libraryVariants
  }
  is com.android.build.gradle.AppExtension -> {
   androidExtension.applicationVariants
  }
  else -> null
 }
}

fun Project.registerKspKotlinOutputAsSourceSet() {
 afterEvaluate {
  val android by lazy {
   extensions.findByType(com.android.build.gradle.BaseExtension::class.java)
    ?: throw NullPointerException()
  }

  requireAndroidVariants().forEach { variant ->
   val variantName = variant.name
   val outputFolder = File("build/generated/ksp/$variantName/kotlin")
   variant.addJavaSourceFoldersToModel(outputFolder)
   android.sourceSets.getAt(variantName).java {
    srcDir(outputFolder)
   }
  }
 }
}
```
