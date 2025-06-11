// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        implementation(project(":core"))
        implementation(libs.kotlinx.serialization)
    }

    sourceSets.jvmMain.dependencies {
        api(project(":api"))
        implementation(project(":core"))
        implementation(kotlin("stdlib"))
        implementation(libs.slf4j.api)
        implementation(libs.mvel)
        implementation(libs.kotlinx.serialization)
        implementation(libs.kaml)
    }
}

