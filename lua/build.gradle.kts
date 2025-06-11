// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        api(project(":api"))
        implementation(project(":core"))
        implementation(libs.korlibs.luak)
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    sourceSets.jvmTest.dependencies {
        implementation(kotlin("test-junit5"))
        runtimeOnly(libs.junit.jupiter.engine)
    }
}

