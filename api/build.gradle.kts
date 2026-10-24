// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {

        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib.common)
                api(libs.okio)
            }
        }

        jvmMain {}

        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit5)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.okio.fakefilesystem)
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations.common)
            }
        }
    }
}

tasks {

    compileKotlinJvm {
        dependsOn("createAraraAPIObject")
    }

    register("createAraraAPIObject") {
        val output = layout.projectDirectory.file("src/jvmMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt")
        val apiVersion = project.version.toString()
        inputs.property("apiVersion", apiVersion)
        outputs.file(output)
        doLast {
            output.asFile.writeText(
                    """
                // SPDX-License-Identifier: BSD-3-Clause
                package org.islandoftex.arara.api

                public actual object AraraAPI {
                    @Suppress("MayBeConst")
                    public actual val version: String = "${apiVersion}"
                }

            """.trimIndent()
            )
        }
    }

}

// Documentation and source artifacts also include the generated version object.
tasks.matching {
    it.name.startsWith("dokkaGenerate") || it.name.endsWith("SourcesJar") || it.name == "sourcesJar"
}.configureEach {
    dependsOn("createAraraAPIObject")
}

// This floor measures this module's own test suite.
kover {
    reports {
        total {
            verify {
                onCheck.set(false)
                rule { minBound(80) }
            }
        }
    }
}
