// SPDX-License-Identifier: BSD-3-Clause
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
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
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    // ----------------------------------------------------------------------
    // define the JVM toolchain (we can also set the vendor, if needed)
    // ----------------------------------------------------------------------
    // jvmToolchain {
    //    languageVersion.set(JavaLanguageVersion.of(toolchain))
    //    vendor.set(JvmVendorSpec.AMAZON)
    // }
    // ----------------------------------------------------------------------
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
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        configurations {
            jvmMainImplementation {
                exclude(group = "com.github.ajalt.mordant", module = "mordant-jvm-ffm")
                exclude(group = "com.github.ajalt.mordant", module = "mordant-jvm-jna")
            }
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
                implementation(libs.mordant.core)
                implementation(libs.clikt.markdown)
                implementation(libs.kotaml)
                implementation(libs.mvel)
                implementation(libs.slf4j.api)
                implementation(libs.log4j.core)
                implementation(libs.log4j.impl)
                runtimeOnly(libs.log4j.json)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
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

        transform<Log4j2PluginsCacheFileTransformer>()
        filesMatching("META-INF/*.kotlin_module") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        filesMatching("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        minimize {
            exclude(dependency("org.jetbrains.kotlin:.*"))
            exclude(dependency("org.apache.logging.log4j:log4j-slf4j2-impl:.*"))
            exclude(dependency("org.apache.logging.log4j:log4j-layout-template-json:.*"))
            exclude(dependency("org.mvel:mvel2:.*"))
            exclude(dependency("com.github.ajalt.mordant:.*"))
        }
    }
}
