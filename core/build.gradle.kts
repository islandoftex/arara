// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    api(project(":api"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version = Versions.kotlinxSerialization)
    implementation(group = "com.charleskorn.kaml", name = "kaml", version = Versions.kaml)
    implementation(group = "org.zeroturnaround", name = "zt-exec", version = Versions.ztExec)
    implementation(group = "org.slf4j", name = "slf4j-api", version = Versions.slf4j)

    testImplementation(group = "io.kotest", name = "kotest-runner-junit5-jvm", version = Versions.kotest)
    testImplementation(group = "io.kotest", name = "kotest-assertions-core-jvm", version = Versions.kotest)
    testRuntimeOnly(group = "org.slf4j", name = "slf4j-simple", version = Versions.slf4j)
}

sourceSets {
    main {
        java { setSrcDirs(listOf("src/main/kotlin")) }
        resources { setSrcDirs(listOf("src/main/resources")) }
    }
    test {
        java { setSrcDirs(listOf("src/test/kotlin")) }
        resources { setSrcDirs(listOf("src/test/resources")) }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xopt-in=org.islandoftex.arara.api.localization.AraraMessages," +
                    "kotlin.time.ExperimentalTime,kotlin.io.path.ExperimentalPathApi")
        }
    }
}
