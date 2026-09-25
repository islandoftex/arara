// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.tapmoc)
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(libs.luaj)
    implementation(libs.kotlin.logging)

    testImplementation(libs.kotlin.test)
}
