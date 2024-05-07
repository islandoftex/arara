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
        implementation("com.soywiz.korlibs.luak:luak:${Versions.luak}")
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    sourceSets.jvmTest.dependencies {
        implementation(kotlin("test-junit5"))
        runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    }
}

