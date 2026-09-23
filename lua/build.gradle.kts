// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(libs.luaj)
    implementation(libs.kotlin.logging)
}
