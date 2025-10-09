// SPDX-License-Identifier: BSD-3-Clause

repositories {
    mavenCentral()
    maven("https://gitlab.com/api/v4/projects/74459964/packages/maven")
    maven("https://gitlab.com/api/v4/projects/74780250/packages/maven")
    maven("https://gitlab.com/api/v4/projects/71753494/packages/maven")
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.support.kobats)
    implementation(libs.support.roastmyjar)
    implementation(libs.support.scribeswan)
}
