// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("multiplatform")
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
                api(libs.okio)
            }
        }

        jvmMain {}

        jvmTest {
            dependencies {
                implementation(libs.okio.fakefilesystem)
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
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

configure<org.jetbrains.dokka.gradle.DokkaExtension> {
    dokkaPublications.named("html") {
        failOnWarning.set(true)
    }
}
