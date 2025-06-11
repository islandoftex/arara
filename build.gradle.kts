// build.gradle.kts

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("multiplatform") version libs.versions.kotlin apply false // Apache 2.0
    kotlin("plugin.serialization") version libs.versions.kotlin apply false // Apache 2.0
    alias(libs.plugins.shadow) apply false // Apache 2.0
    alias(libs.plugins.versions) // Apache 2.0
    alias(libs.plugins.spotlessChangelog) // Apache 2.0
    alias(libs.plugins.dokka) apply false // Apache 2.0
    alias(libs.plugins.detekt) // Apache 2.0
    alias(libs.plugins.spotless) // Apache 2.0
}


tasks {

    named<DependencyUpdatesTask>("dependencyUpdates") {
        resolutionStrategy {
            componentSelection {
                all {
                    fun isNonStable(version: String) = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                            .any { qualifier ->
                                version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                            }
                    if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                        reject("Release candidate")
                    }
                }
            }
        }
        checkForGradleUpdate = false
    }
    
}
