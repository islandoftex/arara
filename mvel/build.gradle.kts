// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()

    sourceSets {

        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib.common)
                implementation(project(":core"))
                implementation(libs.kotlinx.serialization)
            }
        }

        jvmMain {
            dependencies {
                api(project(":api"))
                implementation(project(":core"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.slf4j.api)
                implementation(libs.mvel)
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotaml)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations.common)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit5)
                runtimeOnly(libs.junit.jupiter.engine)
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
                rule { minBound(79) }
            }
        }
    }
}
