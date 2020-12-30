// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

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
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
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
            languageSettings.useExperimentalAnnotation("kotlin.io.path.ExperimentalPathApi")
        }
        val jvmTest by getting {
            languageSettings.useExperimentalAnnotation("kotlin.io.path.ExperimentalPathApi")
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
                implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
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
                        public actual val version: String = "${project.version}"
                    }

                    """.trimIndent()
            )
        }
    }
    /*named("compileKotlinLinuxX64").configure {
        dependsOn("createAraraAPIObject")
    }*/
    named<Test>("jvmTest") {
        useJUnitPlatform()

        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR,
                    TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
        }
    }
}
