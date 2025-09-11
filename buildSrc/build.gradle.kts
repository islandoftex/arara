// SPDX-License-Identifier: BSD-3-Clause

repositories {
    mavenCentral()
    maven("https://gitlab.com/api/v4/projects/71753494/packages/maven")
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.islandoftex:scribeswan:0.1.1")
}
