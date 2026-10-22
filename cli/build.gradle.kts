// SPDX-License-Identifier: BSD-3-Clause
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
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

val nativeRuntime = configurations.getByName("jvmRuntimeClasspath").incoming.artifactView {
    componentFilter {
        it is ModuleComponentIdentifier && it.group == "net.java.dev.jna" && it.module == "jna"
    }
}.files

val stageNativeRuntime = tasks.register<Sync>("stageNativeRuntime") {
    from(nativeRuntime) { rename { "jna.jar" } }
    into(layout.buildDirectory.dir("native"))
}

val nativeArguments = providers.provider {
    listOf("--module-path", nativeRuntime.singleFile.absolutePath,
        "--add-modules=com.sun.jna", "--enable-native-access=com.sun.jna")
}

distributions {
    main {
        val application = files(tasks.named("shadowJar"))
        val startScripts = tasks.register<CreateStartScripts>("startScripts") {
            mainClass = entryPath
            classpath = application
            outputDir = layout.buildDirectory.file("scripts").get().asFile
            applicationName = project.name
            defaultJvmOpts = listOf("--add-modules=com.sun.jna", "--enable-native-access=com.sun.jna")
            doLast {
                // Keep Gradle's platform launchers, including their argument and JAVA_OPTS handling.
                val unixAnchor = "-classpath \"${'$'}CLASSPATH\""
                val windowsAnchor = "-classpath \"%CLASSPATH%\""
                val unix = unixScript.readText()
                val windows = windowsScript.readText()
                check(unixAnchor in unix && windowsAnchor in windows) { "Gradle launcher template changed" }
                unixScript.writeText(unix.replace(unixAnchor,
                    "--module-path \"${'$'}APP_HOME/lib/jna.jar\" $unixAnchor"))
                windowsScript.writeText(windows.replace(windowsAnchor,
                    "--module-path \"%APP_HOME%/lib/jna.jar\" $windowsAnchor"))
            }
        }
        contents {
            from(startScripts) { into("bin") }
            from(application) { into("lib") }
            from(stageNativeRuntime) { into("lib") }
        }
    }
}

tasks {

    withType<JavaExec>().configureEach {
        classpath = classpath.minus(nativeRuntime)
        jvmArgumentProviders.add(CommandLineArgumentProvider { nativeArguments.get() })
    }

    withType<Test>().configureEach {
        classpath = classpath.minus(nativeRuntime)
        jvmArgumentProviders.add(CommandLineArgumentProvider { nativeArguments.get() })
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
        dependencies { exclude(dependency("net.java.dev.jna:jna:.*")) }

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
