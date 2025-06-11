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
        runtimeOnly("net.java.dev.jna:jna:5.13.0")
    }

    sourceSets.jvmTest.dependencies {
        implementation("io.kotest:kotest-runner-junit5-jvm:4.6.3")
        implementation("io.kotest:kotest-assertions-core-jvm:4.6.3")
    }
}

