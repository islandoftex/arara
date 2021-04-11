// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions

plugins {
    id("com.github.johnrengelman.shadow")
    application
    jacoco
}

kotlin {
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("org.islandoftex.arara.api.AraraMessages")
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            languageSettings.useExperimentalAnnotation("kotlin.io.path.ExperimentalPathApi")
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":mvel"))
                implementation(project(":kotlin-dsl"))

                implementation(kotlin("reflect", Versions.kotlin))
                implementation("com.github.ajalt.clikt:clikt:${Versions.clikt}")
                implementation("net.mamoe.yamlkt:yamlkt:${Versions.yamlkt}")
                implementation("org.mvel:mvel2:${Versions.mvel}")
                implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
                implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
                implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4j}")
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

application {
    applicationName = project.name
    mainClassName = "$group.cli.CLIKt"
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
    named<Jar>("shadowJar").configure {
        archiveAppendix.set("with-deps")
        archiveClassifier.set("")
    }
}
