// cli/build.gradle.kts

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass.set("org.islandoftex.arara.cli.CLIKt")
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core"))
                implementation(project(":lua"))
                implementation(libs.kotlin.logging)
            }
        }

        jvmMain{
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

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.jvm)
                implementation(libs.kotest.assertions.jvm)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

}
