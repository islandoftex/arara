// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
}

kotlin {
    explicitApi()

    compilerOptions {
        optIn.addAll(
                "org.islandoftex.arara.api.localization.AraraMessages",
                "kotlin.time.ExperimentalTime"
        )
    }
}

dependencies {
    testImplementation(libs.kotest.runner.jvm)
    testImplementation(libs.kotest.assertions.jvm)
}

tasks {

    compileKotlin {
        dependsOn("createAraraAPIObject")
    }

    register("createAraraAPIObject") {
        listOf(
                "src/main/kotlin/org/islandoftex/arara/api/AraraAPI.kt",
        ).forEach {
            file(it).writeText(
                    """
                // SPDX-License-Identifier: BSD-3-Clause
                package org.islandoftex.arara.api

                /**
                 * Store information about arara's build.
                 */
                public object AraraAPI {
                    /**
                     * arara's version.
                     */
                    @Suppress("MayBeConst")
                    public val version: String = "${project.version}"
                }

            """.trimIndent()
            )
        }
    }
}
