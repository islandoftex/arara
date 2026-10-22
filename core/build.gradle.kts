// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
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
                implementation(libs.kotlin.stdlib.common)
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
                implementation(libs.kotlin.test.junit5)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
                runtimeOnly(libs.slf4j.simple)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations.common)
            }
        }
    }
}

// This floor measures this module's own test suite.
kover {
    reports {
        total {
            verify {
                onCheck.set(false)
                rule { minBound(80) }
            }
        }
    }
}
