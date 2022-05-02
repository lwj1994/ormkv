import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

registerKspKotlinOutputAsSourceSet()
android {
    kotlinOptions {
        jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility(1.8)
        targetCompatibility(1.8)
    }

    compileSdk = 32
    defaultConfig {
        applicationId("com.lwjlol.chunchunsp.demo")
        minSdkVersion(19)
        targetSdkVersion(32)
        versionCode(1)
        versionName("1.0")
        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }
    buildTypes {
        val release = getByName("release")
        release.apply {

            sourceSets {
                getByName("main") {
                    java.srcDir(File("build/generated/ksp/release/kotlin")) // 指定ksp生成目录，否则编译器不会之别生成的代码
                }
            }

            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        val debug = getByName("debug")
        debug.apply {

            sourceSets {
                getByName("main") {
                    java.srcDir(File("build/generated/ksp/debug/kotlin")) // 指定ksp生成目录，否则编译器不会之别生成的代码
                }
            }
        }

    }
}



dependencies {
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("com.tencent:mmkv-static:1.2.8")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation(project(":core"))
    implementation(project(":sharedPreferences"))
    implementation(project(":mmkv"))
    implementation(kotlin("stdlib"))
    ksp(project(":compiler"))
//    kapt(project(":compiler"))
    implementation(project(":compiler"))
}

ksp {
    arg("option1", "value1")
    arg("option2", "value2")
}





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