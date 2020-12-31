// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions

plugins {
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            languageSettings.useExperimentalAnnotation("kotlin.io.path.ExperimentalPathApi")
        }
        val jvmMain by getting {
            dependencies {
                api(project(":api"))
                implementation(project(":core"))
                implementation(kotlin("stdlib-jdk7"))
                implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
                implementation("org.mvel:mvel2:${Versions.mvel}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}")
                implementation("com.charleskorn.kaml:kaml:${Versions.kaml}")
            }
        }
    }
}
