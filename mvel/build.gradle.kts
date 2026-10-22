// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kover)
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
                implementation(project(":core"))
                implementation(libs.kotlinx.serialization)
            }
        }

        jvmMain {
            dependencies {
                api(project(":api"))
                implementation(project(":core"))
                implementation(kotlin("stdlib"))
                implementation(libs.slf4j.api)
                implementation(libs.mvel)
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotaml)
            }
        }
    }
}
