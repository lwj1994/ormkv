[![](https://jitpack.io/v/lwj1994/ormkv.svg)](https://jitpack.io/#lwj1994/ormkv)
![](https://img.shields.io/badge/build-passing-green.svg)
![](https://img.shields.io/badge/license-MIT-orange.svg)

Ormkv is a `Object Relational Mapping` keyValue-saving helper. It automatically generates
object-oriented tool class that access key-value pairs based on the bean class.

## Usage

```gradle
    allprojects {
        repositories {
          ...
          maven { url 'https://jitpack.io'}
        }
    }
    
    dependencies {
	   implementation "com.github.lwj1994.ormkv:core:${latestVersion}""
	   
	   // sharedPreferences
	   implementation "com.github.lwj1994.ormkv:sharedPreferences:${latestVersion}"
	   
	   // option :if you use mmkv
	   implementation "com.github.lwj1994.ormkv:mmkv:${latestVersion}"
	   
	   // if you use kapt
	   kapt "com.github.lwj1994.ormkv:compiler:${latestVersion}"
	   
	   // if you use ksp
	   ksp "com.github.lwj1994.ormkv:compiler:${latestVersion}"
	}
```

### Create key-value store class

```kotlin
package com.lwjlol.ormkv.demo

import android.content.Context
import com.lwjlol.ormkv.mmkv.MmkvHandler
import com.lwjlol.ormkv.sp.SharedPreferencesHandler

object KvStore {
    //  if you user SharedPreferences, you can use SharedPreferencesHandler
    val sharedPreferencesHandler =
        SharedPreferencesHandler(App.context.getSharedPreferences("SP", Context.MODE_PRIVATE))

    //  if you user mmkv, you can use MmkvHandler
    val mmkvHandler = MmkvHandler(com.tencent.mmkv.MMKV.defaultMMKV())
}
```

you can write your custom handler implement `OrmKvHandler` class

```kotlin
class MyHandler : OrmKvHandler {

    fun put(key: String, value: Any) {

    }

    fun get(key: String, default: Any): Any {

    }
}

```

### Create `Entity` class

use `@Entity` annotation this class.

```kotlin
@Entity(
    name = "UserRegistry", // default name is className with suffix 'Registry', such as UserInfoRegistry
    handler = "com.lwjlol.ormkv.demo.KvStore.sharedPreferencesHandler", // the store code with qualifiedName
)
data class UserInfo(
    // name: the real key name
    // defaultValue: the default value
    // enableReset: default true, if false will not reset to defValue in [reset()] function
    @ColumnInfo(name = "realKeyName", defaultValue = "david", enableReset = false)
    val name: String,
    @ColumnInfo(defValue = "12")
    val age: Int,
    @ColumnInfo(defValue = "1")
    val id: Long,
    @ColumnInfo(defValue = "true")
    val isMan: Boolean,
    @Ignore       // ignore this property
    val ignore: Boolean
)
```

### Build and generate Registry class

build project and we will get a Registry class.

```kotlin
/**
 * this class is generated by https://github.com/lwj1994/ormkv for
 * [com.lwjlol.ormkv.demo.UserInfo], Please don't modify it!
 */
public object UserRegistry {
    private val kvHandler: OrmKvHandler = com.lwjlol.ormkv.demo.KvStore.sharedPreferencesHandler


    public var name: String
        get() {
            return kvHandler.get("name", "david") as String
        }
        set(`value`) {
            kvHandler.put("name", value)
        }

    public var id: Long
        get() {
            return kvHandler.get("id", 12312312) as Long
        }
        set(`value`) {
            kvHandler.put("id", value)
        }

    public var isMan: Boolean
        get() {
            return kvHandler.get("isMan", false) as Boolean
        }
        set(`value`) {
            kvHandler.put("isMan", value)
        }

    public fun refresh() {
        _name = kvHandler.get("name", "david") as String
        _id = kvHandler.get("id", "12312312") as Long
        _isMan = kvHandler.get(isMan, false) as Boolean
    }

    /**
     * reset all value to default value
     */
    public fun reset(): Unit {
        name = "david"
        id = 1L
        isMan = true
    }

    public override fun toString(): String = """|name = $name
  |id = $id
  |isMan = $isMan
  """.trimMargin()

    public fun update(model: UserInfo): Unit {
        name = model.name
        id = model.id
        isMan = model.isMan
    }

    public fun toModel(): UserInfo = com.lwjlol.ormkv.demo.UserInfo(
        name = name,
        id = id,
        isMan = isMan
    )
}
```

## Set/get value

then we can use the Registry class set/get value directly.

```kotlin
UserRegistry.name = "Mike"

Log.d(tag, UserRegistry.toString())
Log.d(tag, UserRegistry.name)


// refresh all values
UserRegistry.refresh()
```

## KSP

KSP Generated Sources in IDE Note, that as of the current KSP version generated java sources are
detected by the IDE but NOT generated kotlin sources. This means that generated epoxy kotlin
extensions will not automatically be resolved in the IDE. You must manually configure your source
sets to include ksp generated folders.

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
