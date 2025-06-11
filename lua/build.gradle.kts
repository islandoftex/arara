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
        implementation("com.soywiz.korlibs.luak:luak:3.4.0")
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    sourceSets.jvmTest.dependencies {
        implementation(kotlin("test-junit5"))
        runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    }
}

