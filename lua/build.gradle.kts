// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
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
    implementation(project(":core"))
    // the Lua-based rule and configuration formats reuse helper methods and
    // the configuration model that historically live in the MVEL module;
    // they will move to a dedicated module once the YAML format is removed
    // (api scope because LuaLocalConfiguration returns the shared model)
    api(project(":mvel"))
    implementation(libs.luaj)
    implementation(libs.kotlin.logging)

    testImplementation(libs.kotest.runner.jvm)
    testImplementation(libs.kotest.assertions.jvm)
}
