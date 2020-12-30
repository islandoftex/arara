// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform")
    id("com.github.johnrengelman.shadow")
    application
    jacoco
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("org.islandoftex.arara.api.localization.AraraMessages")
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            languageSettings.useExperimentalAnnotation("kotlin.io.path.ExperimentalPathApi")
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":mvel"))

                implementation(kotlin("reflect", Versions.kotlin))
                implementation("com.github.ajalt.clikt:clikt:${Versions.clikt}")
                implementation("org.mvel:mvel2:${Versions.mvel}")
                implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
                implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
                implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4j}")
                implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.jackson}")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}")
                implementation("io.kotest:kotest-assertions-core-jvm:${Versions.kotest}")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
            }
        }
    }
}

application {
    applicationName = project.name
    mainClassName = "$group.cli.CLIKt"
}

tasks {
    named<JavaExec>("run") {
        if (JavaVersion.current() > JavaVersion.VERSION_1_8) {
            doFirst {
                jvmArgs = listOf(
                        "--module-path", classpath.asPath
                )
            }
        }
    }
    withType<Test> {
        useJUnitPlatform()

        testLogging {
            events(TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR,
                    TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
        }
    }
}
tasks.named<Task>("assembleDist").configure {
    dependsOn("shadowJar", "jacocoTestReport")
}
