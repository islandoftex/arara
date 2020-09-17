// SPDX-License-Identifier: BSD-3-Clause

import org.islandoftex.arara.build.Versions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    jacoco
}

dependencies {
    implementation(project(":core"))
    implementation(project(":mvel"))

    implementation(kotlin("reflect", Versions.kotlin))
    implementation(group = "com.github.ajalt.clikt", name = "clikt", version = Versions.clikt)
    implementation(group = "org.mvel" , name = "mvel2", version = Versions.mvel)
    implementation(group = "org.slf4j", name = "slf4j-api", version = Versions.slf4j)
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = Versions.log4j)
    implementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = Versions.log4j)
    implementation(group = "com.fasterxml.jackson.dataformat", name = "jackson-dataformat-yaml", version = Versions.jackson)
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = Versions.jackson)

    testImplementation(group = "io.kotest", name = "kotest-runner-junit5-jvm", version = Versions.kotest)
    testImplementation(group = "io.kotest", name = "kotest-assertions-core-jvm", version = Versions.kotest)
}

val moduleName = group

sourceSets {
    main {
        java { setSrcDirs(listOf("src/main/kotlin")) }
        resources { setSrcDirs(listOf("src/main/resources")) }
    }
    test {
        java { setSrcDirs(listOf("src/test/kotlin")) }
        resources { setSrcDirs(listOf("src/test/resources")) }
    }
}

application {
    applicationName = project.name
    mainClassName = "$moduleName.cli.CLIKt"
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xopt-in=org.islandoftex.arara.api.localization.AraraMessages,kotlin.time.ExperimentalTime,kotlin.RequiresOptIn")
        }
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
}
tasks.named<Task>("assembleDist").configure {
    dependsOn("shadowJar", "jacocoTestReport")
}
