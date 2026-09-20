// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    compilerOptions {
        optIn.addAll(
                "org.islandoftex.arara.api.localization.AraraMessages",
                "kotlin.time.ExperimentalTime",
                "kotlin.ExperimentalUnsignedTypes"
        )
    }
}

dependencies {
    api(project(":api"))
    implementation(libs.kotlin.logging)
    implementation(libs.yamlkt)
    implementation(libs.ztexec)

    testImplementation(libs.kotest.runner.jvm)
    testImplementation(libs.kotest.assertions.jvm)
    testRuntimeOnly(libs.slf4j.simple)
}
