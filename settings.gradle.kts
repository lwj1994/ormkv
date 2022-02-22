rootProject.name = "ormkv"
include(":app")
include(":compiler")
include(":core")
include(":sharedPreferences")
include(":mmkv")


pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
    }
    repositories {
        gradlePluginPortal()
    }
}
