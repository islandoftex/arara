// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets.all {
        dependencies {
            api(project(":api"))
            implementation(libs.kotlin.logging)
        }
    }

    sourceSets.commonMain.dependencies {
        implementation(libs.korlibs.korio)
        implementation(libs.yamlkt)
    }

    sourceSets.jvmMain.dependencies {
        implementation(libs.ztexec)
        implementation(libs.korlibs.korio)
    }

    sourceSets.jvmTest.dependencies {
        implementation(libs.kotest.runner.jvm)
        implementation(libs.kotest.assertions.jvm)
        runtimeOnly(libs.slf4j.simple)
    }
}

