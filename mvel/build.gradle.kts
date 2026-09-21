// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(libs.slf4j.api)
    implementation(libs.mvel)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotaml)
}
