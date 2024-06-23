// SPDX-License-Identifier: BSD-3-Clause

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
    application
    jacoco
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":lua"))
                implementation(libs.kotlin.logging)
            }
        }
        val jvmMain by getting {
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
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
            }
        }
    }
}

application {
    applicationName = project.name
    mainClass.set("$group.cli.CLIKt")
}

tasks {
    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    named<JavaExec>("run") {
        if (JavaVersion.current() > JavaVersion.VERSION_1_8) {
            doFirst {
                jvmArgs = listOf(
                    "--module-path", classpath.asPath
                )
            }
        }
    }
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
        }
    }
}
