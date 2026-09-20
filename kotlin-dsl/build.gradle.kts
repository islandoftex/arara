// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        optIn.addAll(
                "org.islandoftex.arara.api.localization.AraraMessages",
                "kotlin.time.ExperimentalTime"
        )
    }
}

dependencies {
    api(project(":api"))
    api(kotlin("scripting-jvm"))
    api(kotlin("scripting-jvm-host"))
    implementation(project(":core"))
    runtimeOnly(kotlin("compiler-embeddable"))
    runtimeOnly(kotlin("scripting-dependencies"))
    runtimeOnly(libs.jna)

    testImplementation(libs.kotest.runner.jvm)
    testImplementation(libs.kotest.assertions.jvm)
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
