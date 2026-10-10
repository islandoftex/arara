// SPDX-License-Identifier: BSD-3-Clause
plugins {
    `kotlin-dsl`
}

// the Kotlin plugin's own stdlib and reflect coordinates resolve
// through here, and so does the helper build's dependencies, so the
// repositories declaration is load-bearing
repositories {
    mavenCentral()
    // the scenario generator library ships on the group registry
    maven {
        url = uri("https://gitlab.com/api/v4/projects/74459964/packages/maven")
    }
    // the markdown conversion library ships on the group registry
    maven {
        url = uri("https://gitlab.com/api/v4/projects/85981688/packages/maven")
    }
    // the diagram rendering client ships on the group registry
    maven {
        url = uri("https://gitlab.com/api/v4/projects/87423722/packages/maven")
    }
    // the SVG to PDF transcoder ships on the group registry
    maven {
        url = uri("https://gitlab.com/api/v4/projects/87464209/packages/maven")
    }
}

dependencies {
    implementation(libs.kobats)
    implementation(libs.coralmark)
    implementation(libs.crocplot)
    implementation(libs.svgecko)
}
