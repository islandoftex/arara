// SPDX-License-Identifier: BSD-3-Clause
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.shadow)
    distribution
}

val entryPath = "org.islandoftex.arara.cli.CLIKt"

kotlin {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

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
            }
        }

        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib.common)
                implementation(project(":core"))
                implementation(project(":lua"))
                implementation(libs.kotlin.logging)
            }
        }

        jvmMain {
            dependencies {
                implementation(project(":core"))
                implementation(project(":mvel"))
                implementation(libs.kotlin.reflect)
                implementation(libs.clikt)
                implementation(libs.mordant.core)
                runtimeOnly(libs.mordant.jna)
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
                implementation(libs.kotlin.test.junit5)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations.common)
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

        val startScripts = tasks.register<CreateStartScripts>("startScripts") {
            mainClass = entryPath
            classpath = jvmRuntimeClasspath
            outputDir = layout.buildDirectory.file("scripts").get().asFile
            applicationName = project.name
            defaultJvmOpts = listOf("--enable-native-access=ALL-UNNAMED")
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

    withType<JavaExec>().configureEach {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }

    withType<Test>().configureEach {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }

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
        // java -jar needs the same native terminal access as the generated launchers.
        manifest.attributes["Enable-Native-Access"] = "ALL-UNNAMED"

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
            exclude(dependency("net.java.dev.jna:.*"))
        }
    }
}

// This floor measures this module's own test suite.
kover {
    reports {
        total {
            verify {
                onCheck.set(false)
                rule { minBound(90) }
            }
        }
    }
}
