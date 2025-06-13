// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    sourceSets {

        all {
            languageSettings.optIn("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        jvmMain {
            dependencies {
                api(project(":api"))
                api(kotlin("scripting-jvm"))
                api(kotlin("scripting-jvm-host"))
                implementation(project(":core"))
                runtimeOnly(kotlin("compiler-embeddable"))
                runtimeOnly(kotlin("scripting-dependencies"))
                runtimeOnly(libs.jna)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
            }
        }
    }
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
