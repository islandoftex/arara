// SPDX-License-Identifier: BSD-3-Clause

plugins {
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(libs.kotlinx.serialization)
            }
        }
        val jvmMain by getting {
            dependencies {
                api(project(":api"))
                implementation(project(":core"))
                implementation(kotlin("stdlib"))
                implementation(libs.slf4j.api)
                implementation(libs.mvel)
                implementation(libs.kotlinx.serialization)
                implementation(libs.kaml)
            }
        }
    }
}
