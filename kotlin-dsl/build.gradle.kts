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
        runtimeOnly(libs.jna)
    }

    sourceSets.jvmTest.dependencies {
        implementation(libs.kotest.runner.jvm)
        implementation(libs.kotest.assertions.jvm)
    }
}
