// cli/build.gradle.kts

@file:OptIn(
        ExperimentalKotlinGradlePluginApi::class,
        ExperimentalKotlinGradlePluginApi::class
)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.shadow)
    jacoco
}

kotlin {

    // this seems interesting to have here
    jvmToolchain(11)

    jvm {

        mainRun {
            mainClass.set("org.islandoftex.arara.cli.CLIKt")
        }

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass.set("org.islandoftex.arara.cli.CLIKt")
            }
        }
    }

    sourceSets {

        all {
            with(languageSettings) {
                optIn("org.islandoftex.arara.api.localization.AraraMessages")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.RequiresOptIn")
            }
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
//                implementation(libs.cliktMarkdown)
                implementation(libs.yamlkt)
                implementation(libs.mvel)
                implementation(libs.slf4j.api)
                implementation(libs.kotlin.logging)
                implementation(libs.log4j.core)
                implementation(libs.log4j.impl)
                // looks like we need this
                implementation(libs.slf4j.simple)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
//                runtimeOnly(libs.slf4j.simple)
                
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks {

    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

//    named<JavaExec>("run") {
//        if (JavaVersion.current() > JavaVersion.VERSION_1_8) {
//            doFirst {
//                jvmArgs = listOf(
//                        "--module-path", classpath.asPath
//                )
//            }
//        }
//    }
//
    named<Task>("assembleDist").configure {
        dependsOn("shadowJar", "jacocoTestReport")
    }

    build {
        dependsOn("shadowJar")
    }

    named<ShadowJar>("shadowJar").configure {

        archiveAppendix.set("with-deps")
        archiveClassifier.set("")

        manifest {
            attributes["Main-Class"] = "org.islandoftex.arara.cli.CLIKt"
        }

        minimize {
            exclude(dependency("org.jetbrains.kotlin:.*"))
            exclude(dependency("org.apache.logging.log4j:log4j-slf4j-impl:.*"))
            exclude(dependency("org.mvel:mvel2:.*"))
            exclude(dependency("net.java.dev.jna:.*:.*"))
            exclude(dependency("com.github.ajalt.mordant:.*"))
        }
    }
}
