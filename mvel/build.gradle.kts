// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.tapmoc)
    alias(libs.plugins.kover)
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(libs.slf4j.api)
    implementation(libs.mvel)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotaml)
    testImplementation(libs.kotlin.test)
}

// the module's own test suite is what its report measures - the
// cross-module coverage of these classes is guarded by the aggregate
// floor at the root. This floor sits below the module's own measured
// baseline, so a regression in the module suite fails the gate
kover {
    reports {
        verify {
            rule {
                minBound(65)
            }
        }
    }
}
