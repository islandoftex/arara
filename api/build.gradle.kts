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

// the version object is generated in place at configuration time:
// every reader of the module's main sources - the compiler, the
// linter, the formatter, the documentation generator - sees the file
// without a single ordering wire, and no reader can race a write it
// never observes; the project version is a configuration input, so
// a version change always refreshes the file before any task runs.
// The tracked copy stays an honest diff of every version bump.
val araraVersion = project.version
file("src/main/kotlin/org/islandoftex/arara/api/AraraAPI.kt").writeText(
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
        public val version: String = "$araraVersion"
    }

    """.trimIndent()
)

// the module's own test suite is what its report measures - the
// cross-module coverage of these classes is guarded by the aggregate
// floor at the root. This floor sits below the module's own measured
// baseline, so a regression in the module suite fails the gate
kover {
    reports {
        verify {
            rule {
                minBound(72)
            }
        }
    }
}
