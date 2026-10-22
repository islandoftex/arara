// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {

        all {
            languageSettings.apply {
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.ExperimentalUnsignedTypes")
            }
        }

        commonMain {
            dependencies {
                api(project(":api"))
                implementation(libs.kotlin.logging)
                implementation(libs.kotaml)
                implementation(libs.kotlinx.serialization)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ztexec)
                runtimeOnly(libs.slf4j.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}
