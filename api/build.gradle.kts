// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(libs.okio)

    testImplementation(libs.okio.fakefilesystem)
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
