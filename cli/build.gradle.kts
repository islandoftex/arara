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
    implementation(group = "com.uchuhimo", name = "konf-core", version = Versions.konf)
    implementation(group = "com.github.ajalt", name = "clikt", version = Versions.clikt)
    implementation(group = "ch.qos.logback", name = "logback-classic", version = Versions.logback)
    implementation(group = "ch.qos.logback", name = "logback-core", version = Versions.logback)
    implementation(group = "org.mvel" , name = "mvel2", version = Versions.mvel)
    implementation(group = "org.slf4j", name = "slf4j-api", version = Versions.slf4j)
    implementation(group = "com.fasterxml.jackson.dataformat", name = "jackson-dataformat-yaml", version = Versions.jackson)
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = Versions.jackson)
    implementation(group = "org.zeroturnaround", name = "zt-exec", version = Versions.ztExec)

    testImplementation(group = "io.kotest", name = "kotest-runner-junit5-jvm", version = Versions.kotest)
    testImplementation(group = "io.kotest", name = "kotest-assertions-core-jvm", version = Versions.kotest)
}

val moduleName = group

sourceSets {
    main {
        java { setSrcDirs(listOf("src/main/java", "src/main/kotlin")) }
        resources { setSrcDirs(listOf("src/main/resources")) }
    }
    test {
        java { setSrcDirs(listOf("src/test/kotlin")) }
        resources { setSrcDirs(listOf("src/test/resources")) }
    }
}

application {
    applicationName = project.name
    mainClassName = "$moduleName.Arara"
}

tasks {
    named<JavaCompile>("compileJava") {
        if (java.sourceCompatibility > JavaVersion.VERSION_1_8) {
            inputs.property("moduleName", moduleName)
            options.compilerArgs = listOf(
                    // include Gradle dependencies as modules
                    "--module-path", sourceSets["main"].compileClasspath.asPath)
        }
    }
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
