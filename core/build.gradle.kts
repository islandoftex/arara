// SPDX-License-Identifier: BSD-3-Clause

plugins {
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    /*wasm32()
    js {
        browser {
            testTask {
                enabled = false
            }
        }
    }
    linuxArm64()
    linuxX64()
    macosX64()
    mingwX64()*/

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
        val commonMain by getting {
            dependencies {
                implementation(libs.korlibs.korio)
                implementation(libs.yamlkt)
            }
        }
        /*val nativeCommonMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeCommonMain)
        }*/
        val jvmMain by getting {
            dependencies {
                implementation(libs.ztexec)
                implementation(libs.korlibs.korio)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}
