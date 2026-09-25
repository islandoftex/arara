// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.tapmoc)
    alias(libs.plugins.kover)
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
    implementation(libs.kotlinx.serialization)
    implementation(libs.okio)
    implementation(libs.zt.exec)

    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.slf4j.simple)
}
