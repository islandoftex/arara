// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions

kotlin {
    explicitApi()

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
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api("com.soywiz.korlibs.klock:klock:${Versions.klock}")
            }
        }
        /*val nativeCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:${Versions.coroutines}")
                implementation("com.soywiz.korlibs.korio:korio:${Versions.korlibs}")
            }
        }
        val linuxX64Main by getting {
            dependsOn(nativeCommonMain)
        }*/
        val jvmMain by getting {
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio:${Versions.korlibs}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
                implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
            }
        }
    }
}

tasks {
    create("createAraraAPIObject") {
        listOf(
            "src/nativeCommonMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt",
            "src/jvmMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt"
        ).forEach {
            file(it).writeText(
                """
                    // SPDX-License-Identifier: BSD-3-Clause
                    package org.islandoftex.arara.api

                    public actual object AraraAPI {
                        @Suppress("MayBeConst")
                        public actual val version: String = "${project.version}"
                    }

                """.trimIndent()
            )
        }
    }
    /*named("compileKotlinLinuxX64").configure {
        dependsOn("createAraraAPIObject")
    }*/
}
