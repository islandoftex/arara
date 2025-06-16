// SPDX-License-Identifier: BSD-3-Clause
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.shadow)
    distribution
}

val entryPath = "org.islandoftex.arara.cli.CLIKt"

// ----------------------------------------------------------------------
// The JVM toolchain, with its corresponding default as fallback, defined
// in gradle.properties (see key)
// ----------------------------------------------------------------------
val toolchain = runCatching { (extra["arara.jvm.target"] as String).toInt() }
        .getOrDefault(11)
// ----------------------------------------------------------------------

kotlin {

    // define the JVM toolchain
    jvmToolchain(toolchain)

    jvm {

        mainRun {
            mainClass.set(entryPath)
        }

        binaries {
            executable {
                mainClass.set(entryPath)
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        commonMain {
            dependencies {
                implementation(project(":core"))
                implementation(project(":lua"))
                implementation(libs.kotlin.logging)
            }
        }

        jvmMain {
            dependencies {
                implementation(project(":core"))
                implementation(project(":mvel"))
                implementation(kotlin("reflect", libs.versions.kotlin.get()))
                implementation(libs.clikt)
                implementation(libs.clikt.markdown)
                implementation(libs.yamlkt)
                implementation(libs.mvel)
                implementation(libs.slf4j.api)
                implementation(libs.log4j.core)
                implementation(libs.log4j.impl)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
            }
        }
    }
}

distributions {
    main {
        val jvmRuntimeClasspath = files(
                tasks.named("jvmJar"),
                configurations.getByName("jvmRuntimeClasspath"),
        )

        val startScripts by tasks.registering(CreateStartScripts::class) {
            mainClass = entryPath
            classpath = jvmRuntimeClasspath
            outputDir = layout.buildDirectory.file("scripts").get().asFile
            applicationName = project.name
        }

        contents {
            from(startScripts) {
                into("bin")
            }
            from(jvmRuntimeClasspath) {
                into("lib")
            }
        }
    }
}

tasks {

    build {
        dependsOn("shadowJar")
    }

    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    named<Task>("assembleDist").configure {
        dependsOn("shadowJar")
    }

    named<ShadowJar>("shadowJar").configure {
        archiveAppendix.set("with-deps")
        archiveClassifier.set("")

        minimize {
            exclude(dependency("org.jetbrains.kotlin:.*"))
            exclude(dependency("org.apache.logging.log4j:log4j-slf4j2-impl:.*"))
            exclude(dependency("org.mvel:mvel2:.*"))
            exclude(dependency("net.java.dev.jna:.*:.*"))
            exclude(dependency("com.github.ajalt.mordant:.*"))
        }
    }
}
