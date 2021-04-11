// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("org.islandoftex.arara.api.Incubating")
                useExperimentalAnnotation("org.islandoftex.arara.api.AraraMessages")
                useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            }
        }
        val jvmMain by getting {
            dependencies {
                api(project(":api"))
                api(kotlin("scripting-jvm"))
                api(kotlin("scripting-jvm-host"))

                implementation(project(":core"))
                implementation(kotlin("script-util"))

                runtimeOnly(kotlin("compiler-embeddable"))
                runtimeOnly(kotlin("scripting-dependencies"))
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

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
