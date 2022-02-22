val kspVersion: String by project

plugins {
    kotlin("jvm")
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation(project(":core"))
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview"
}

apply(from = "../deploy.gradle")