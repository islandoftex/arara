// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions

plugins {
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        val commonMain by getting {
            dependencies {
                api(project(":api"))
                implementation(project(":core"))
                implementation("com.soywiz.korlibs.luak:luak:${Versions.luak}")
            }
        }
    }
}
