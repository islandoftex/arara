// SPDX-License-Identifier: BSD-3-Clause

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        val jvmMain by getting {
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
        val jvmTest by getting {
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
