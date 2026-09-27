// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.tapmoc)
    alias(libs.plugins.kover)
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(libs.okio)

    testImplementation(libs.kotlin.test)
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

// the module's own test suite is what its report measures - the
// cross-module coverage of these classes is guarded by the aggregate
// floor at the root. This floor sits below the module's own measured
// baseline, so a regression in the module suite fails the gate
kover {
    reports {
        verify {
            rule {
                minBound(45)
            }
        }
    }
}
