// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(group = "org.slf4j", name = "slf4j-api", version = Versions.slf4j)
    implementation(group = "org.mvel" , name = "mvel2", version = Versions.mvel)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version = Versions.kotlinxSerialization)
    implementation(group = "com.charleskorn.kaml", name = "kaml", version = Versions.kaml)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xopt-in=org.islandoftex.arara.api.localization.AraraMessages," +
                    "kotlin.time.ExperimentalTime,kotlin.io.path.ExperimentalPathApi")
        }
    }
}
