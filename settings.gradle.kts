// SPDX-License-Identifier: BSD-3-Clause
pluginManagement {
    repositories {
        // the leopack bridge: 0.1.1 is consumed from the local Maven
        // repository until the plugin is published to the GitLab
        // registry; remove this line once that happens
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        // the chronoheader plugin registry
        maven {
            url = uri("https://gitlab.com/api/v4/projects/85815049/packages/maven")
        }
        // the solstice plugin registry
        maven {
            url = uri("https://gitlab.com/api/v4/projects/87038355/packages/maven")
        }
        // the leopack plugin registry
        maven {
            url = uri("https://gitlab.com/api/v4/projects/86391394/packages/maven")
        }
        // the scribeswan registry, consumed by the leopack companion
        maven {
            url = uri("https://gitlab.com/api/v4/projects/71753494/packages/maven")
        }
    }
}

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
