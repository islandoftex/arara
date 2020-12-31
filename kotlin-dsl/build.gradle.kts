// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions

plugins {
    application
}

kotlin {
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }
        val jvmMain by getting {
            dependencies {
                api(project(":api"))
                implementation(project(":core"))
                implementation(kotlin("script-util"))
                implementation(kotlin("scripting-jvm"))
                implementation(kotlin("scripting-jvm-host"))
                runtimeOnly(kotlin("compiler-embeddable"))
                runtimeOnly(kotlin("scripting-compiler-embeddable"))
                runtimeOnly("net.java.dev.jna:jna:${Versions.jna}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
                implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
            }
        }
    }
}

// this is temporarily a full-featured application
application {
    applicationName = project.name
    mainClassName = "org.islandoftex.arara.dsl.Executor"
}
