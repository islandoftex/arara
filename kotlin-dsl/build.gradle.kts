// SPDX-License-Identifier: BSD-3-Clause

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.islandoftex.arara.build.Versions
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    // `java-library`
    application
}

// this is temporarily a full-featured application
application {
    applicationName = project.name
    mainClassName = "org.islandoftex.arara.dsl.Executor"
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(kotlin("script-util", Versions.kotlin))
    implementation(kotlin("scripting-jvm", Versions.kotlin))
    implementation(kotlin("scripting-jvm-host-embeddable", Versions.kotlin))
    runtimeOnly(kotlin("compiler-embeddable", Versions.kotlin))
    runtimeOnly(kotlin("scripting-compiler-embeddable", Versions.kotlin))
    runtimeOnly(group = "net.java.dev.jna", name = "jna", version = Versions.jna)

    testImplementation(group = "io.kotest", name = "kotest-runner-junit5-jvm", version = Versions.kotest)
    testImplementation(group = "io.kotest", name = "kotest-assertions-core-jvm", version = Versions.kotest)
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
