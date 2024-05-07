// core/build.gradle.kts
plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    sourceSets.jvmMain.dependencies {
        api(project(":api"))
        api(kotlin("scripting-jvm"))
        api(kotlin("scripting-jvm-host"))

        implementation(project(":core"))

        runtimeOnly(kotlin("compiler-embeddable"))
        runtimeOnly(kotlin("scripting-dependencies"))
        runtimeOnly("net.java.dev.jna:jna:${Versions.jna}")
    }

    sourceSets.jvmTest.dependencies {
        implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
        implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
    }
}

