// SPDX-License-Identifier: BSD-3-Clause
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.shadow)
    jacoco
}

kotlin {

    // define the JVM toolchain
    jvmToolchain(11)

    jvm {

        mainRun {
            mainClass.set("org.islandoftex.arara.cli.CLIKt")
        }

        binaries {
            executable {
                mainClass.set("org.islandoftex.arara.cli.CLIKt")
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
                implementation(libs.yamlkt)
                implementation(libs.mvel)
                implementation(libs.slf4j.api)
                implementation(libs.kotlin.logging)
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

tasks {

    build {
        dependsOn("shadowJar")
    }

    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

// TODO remove later?
//    named<JavaExec>("run") {
//        if (JavaVersion.current() > JavaVersion.VERSION_1_8) {
//            doFirst {
//                jvmArgs = listOf(
//                    "--module-path", classpath.asPath
//                )
//            }
//        }
//    }

    named<Task>("assembleDist").configure {
        dependsOn("shadowJar", "jacocoTestReport")
    }

    named<ShadowJar>("shadowJar").configure {
        archiveAppendix.set("with-deps")
        archiveClassifier.set("")

        minimize {
            exclude(dependency("org.jetbrains.kotlin:.*"))
            exclude(dependency("org.apache.logging.log4j:log4j-slf4j-impl:.*"))
            exclude(dependency("org.mvel:mvel2:.*"))
            exclude(dependency("net.java.dev.jna:.*:.*"))
            exclude(dependency("com.github.ajalt.mordant:.*"))
       }
    }
}
