// SPDX-License-Identifier: BSD-3-Clause
plugins {
    // the settings plugin block has no access to the project catalog's
    // accessors, so the toolchain resolver declaration stays inline
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "arara"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    "api",
    "core",
    "lua",
    "mvel",
    "cli"
)
