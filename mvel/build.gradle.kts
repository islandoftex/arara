// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {

        all {
            languageSettings.optIn("org.islandoftex.arara.api.localization.AraraMessages")
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
                implementation(libs.kaml)
            }
        }
    }
}
