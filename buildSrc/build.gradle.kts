// SPDX-License-Identifier: BSD-3-Clause
plugins {
    `kotlin-dsl`
}

// the Kotlin plugin's own stdlib and reflect coordinates resolve
// through here, and so does the helper build's one dependency, so the
// repositories declaration is load-bearing
repositories {
    mavenCentral()
    // the scenario generator library ships on the group registry
    maven {
        url = uri("https://gitlab.com/api/v4/projects/74459964/packages/maven")
    }
}

dependencies {
    implementation("org.islandoftex:kobats:0.2.1")
}
