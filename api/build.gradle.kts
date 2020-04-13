// SPDX-License-Identifier: BSD-3-Clause

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
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
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}
