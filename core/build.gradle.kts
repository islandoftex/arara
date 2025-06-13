// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {

        all {
            languageSettings.apply {
                optIn("org.islandoftex.arara.api.localization.AraraMessages")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.ExperimentalUnsignedTypes")
            }

            dependencies {
                api(project(":api"))
                implementation(libs.kotlin.logging)
            }
        }

        commonMain {
            dependencies {
                implementation(libs.korlibs.korio)
                implementation(libs.yamlkt)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ztexec)
                implementation(libs.korlibs.korio)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
                runtimeOnly(libs.slf4j.simple)
            }
        }

// TODO add this?
//        commonTest {
//            dependencies {
//                implementation(kotlin("test"))
//            }
//        }
    }
}
