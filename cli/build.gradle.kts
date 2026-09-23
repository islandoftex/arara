// SPDX-License-Identifier: BSD-3-Clause
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
    application
}

val entryPath = "org.islandoftex.arara.cli.CLIKt"

// ----------------------------------------------------------------------
// The JVM toolchain, with its corresponding default as fallback, defined
// in gradle.properties (see key)
// ----------------------------------------------------------------------
val toolchain = providers.gradleProperty("arara.jvm.target")
        .map { it.toInt() }
        .getOrElse(11)
// ----------------------------------------------------------------------

kotlin {
    jvmToolchain(toolchain)
}

application {
    mainClass.set(entryPath)
    applicationName = project.name
}

configurations {
    implementation {
        exclude(group = "com.github.ajalt.mordant", module = "mordant-jvm-ffm")
        exclude(group = "com.github.ajalt.mordant", module = "mordant-jvm-jna")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotaml)
    implementation(project(":lua"))
    implementation(project(":mvel"))
    // the command line framework, the YAML parser, the serialization
    // runtime and the logging facade reference the Kotlin reflection
    // classes at runtime, but none of them declares the dependency, so
    // this line is the sole provider of it; removing it fails the CLI
    // startup and the YAML parsing (verified by observing the class
    // loads at runtime)
    implementation(kotlin("reflect"))
    implementation(libs.kotlin.logging)
    implementation(libs.clikt)
    implementation(libs.clikt.markdown)
    implementation(libs.mvel)
    implementation(libs.slf4j.api)
    implementation(libs.log4j.core)
    implementation(libs.log4j.impl)

}

tasks {

    build {
        dependsOn("shadowJar")
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
            exclude(dependency("com.github.ajalt.mordant:.*"))
        }
    }
}
