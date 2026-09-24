// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.ExperimentalUnsignedTypes")
    }
}

dependencies {
    api(project(":api"))
    implementation(libs.kotlin.logging)
    implementation(libs.kotaml)
    implementation(libs.okio)
    implementation(libs.ztexec)

    testRuntimeOnly(libs.slf4j.simple)
}
