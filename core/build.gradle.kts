// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions

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
    linuxX64()

    /*wasm32()
    js {
        browser {
            testTask {
                enabled = false
            }
        }
    }
    linuxArm64()
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
                implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio:${Versions.korlibs}")
                implementation("net.mamoe.yamlkt:yamlkt:${Versions.yamlkt}")
            }
        }
        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeCommonMain)
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.zeroturnaround:zt-exec:${Versions.ztExec}")
                implementation("com.soywiz.korlibs.korio:korio:${Versions.korlibs}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
                implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
                runtimeOnly("org.slf4j:slf4j-simple:${Versions.slf4j}")
            }
        }
    }
}
