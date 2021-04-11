// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions

plugins {
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("org.islandoftex.arara.api.Incubating")
                useExperimentalAnnotation("org.islandoftex.arara.api.AraraMessages")
                useExperimentalAnnotation("kotlin.time.ExperimentalTime")
                useExperimentalAnnotation("kotlin.io.path.ExperimentalPathApi")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}")
            }
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
