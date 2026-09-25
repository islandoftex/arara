// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.tapmoc)
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(libs.slf4j.api)
    implementation(libs.mvel)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotaml)
}
