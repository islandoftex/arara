// SPDX-License-Identifier: BSD-3-Clause

kotlin {
    explicitApi()

    jvm()
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
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api(libs.korlibs.klock)
            }
        }
        /*val nativeCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.coroutines)
                implementation(libs.korlibs.korio)
            }
        }
        val linuxX64Main by getting {
            dependsOn(nativeCommonMain)
        }*/
        val jvmMain by getting {
            dependencies {
                implementation(libs.korlibs.korio)
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
