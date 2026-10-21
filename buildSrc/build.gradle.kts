// SPDX-License-Identifier: BSD-3-Clause

repositories {
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(gradleTestKit())
}

val websiteToolsTest = tasks.register<JavaExec>("websiteToolsTest") {
    group = "verification"
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("org.islandoftex.arara.build.website.WebsiteToolsChecksKt")
    args(projectDir.parentFile.absolutePath)
    inputs.dir(projectDir.parentFile.resolve("website/content"))
    inputs.dir(projectDir.parentFile.resolve("website/themes/devlab-theme"))
}

// Regression checks use a standalone Kotlin main rather than a JUnit engine.
tasks.test { failOnNoDiscoveredTests.set(false) }
tasks.check { dependsOn(websiteToolsTest) }

val packagingTest = tasks.register<JavaExec>("packagingTest") {
    group = "verification"
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("org.islandoftex.arara.build.PackagingChecksKt")
    args(gradle.gradleHomeDir!!.absolutePath, sourceSets.main.get().output.classesDirs.asPath)
}
tasks.check { dependsOn(packagingTest) }
