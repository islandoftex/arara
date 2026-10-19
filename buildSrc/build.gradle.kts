// SPDX-License-Identifier: BSD-3-Clause

repositories {
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

val websiteToolsTest by tasks.registering(JavaExec::class) {
    group = "verification"
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("org.islandoftex.arara.build.website.WebsiteToolsChecksKt")
    args(projectDir.parentFile.absolutePath)
    inputs.dir(projectDir.parentFile.resolve("website/content"))
    inputs.dir(projectDir.parentFile.resolve("website/themes/devlab-theme"))
}

tasks.check { dependsOn(websiteToolsTest) }
