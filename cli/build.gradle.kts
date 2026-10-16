// SPDX-License-Identifier: BSD-3-Clause
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.tapmoc)
    alias(libs.plugins.kover)
    alias(libs.plugins.shadow)
    application
}

val entryPath = "org.islandoftex.arara.cli.CLIKt"

application {
    mainClass.set(entryPath)
    applicationName = project.name
}

configurations {
    implementation {
        // the foreign-function mapping module needs a Java the
        // eleven-floor runtime cannot provide, so the detection
        // rides JNA alone - the kernel's isatty answer instead of
        // the environment's guess
        exclude(group = "com.github.ajalt.mordant", module = "mordant-jvm-ffm")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":lua"))
    implementation(project(":mvel"))
    implementation(libs.kotaml)
    // the command line framework, the YAML parser, the serialization
    // runtime and the logging facade reference the Kotlin reflection
    // classes at runtime, but none of them declares the dependency, so
    // this line is the sole provider of it; removing it fails the CLI
    // startup and the YAML parsing (verified by observing the class
    // loads at runtime)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.logging)
    implementation(libs.clikt)
    implementation(libs.clikt.markdown)
    implementation(libs.mordant)
    // the terminal library's detection rides JNA internally; the
    // stream adaptation borrows the console codepage through the
    // same library, and the exclusion tree above keeps the two
    // in step
    implementation(libs.jna)
    implementation(libs.mvel)
    implementation(libs.slf4j.api)
    implementation(libs.log4j.core)
    implementation(libs.log4j.impl)
    implementation(libs.zt.exec)

    testImplementation(libs.kotlin.test)
}

tasks {

    build {
        dependsOn("shadowJar")
    }

    named<ShadowJar>("shadowJar").configure {
        archiveAppendix.set("with-deps")
        archiveClassifier.set("")

        // the terminal library binds through JNA, whose dispatch
        // load trips the native-access guard the newest runtimes
        // keep on restricted calls; the manifest attribute
        // declares the jar's intent and the launcher grants it,
        // so the invocation opens clean wherever the guard stands
        manifest {
            attributes("Enable-Native-Access" to "ALL-UNNAMED")
        }

        minimize {
            exclude(dependency("org.jetbrains.kotlin:.*"))
            exclude(dependency("org.apache.logging.log4j:log4j-slf4j2-impl:.*"))
            exclude(dependency("org.mvel:mvel2:.*"))
            exclude(dependency("com.github.ajalt.mordant:.*"))
        }

        // the Kotlin metadata entries are handled by the merger
        // transformer; the default duplicate strategy would silently
        // drop same-named entries before the transformer sees them,
        // so they warn and process instead — the healthy dependency
        // graph carries no such duplicates, and a warning marks any
        // that ever appear
        filesMatching("META-INF/*.kotlin_module") {
            duplicatesStrategy = DuplicatesStrategy.WARN
        }
    }
}

// the module's own test suite is what its report measures - the
// cross-module coverage of these classes is guarded by the aggregate
// floor at the root. This floor sits below the module's own measured
// baseline, so a regression in the module suite fails the gate
kover {
    reports {
        verify {
            rule {
                minBound(86)
            }
        }
    }
}
