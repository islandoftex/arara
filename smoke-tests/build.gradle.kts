// SPDX-License-Identifier: BSD-3-Clause
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(providers.gradleProperty("arara.jvm.target").get().toInt())
}

dependencies {
    implementation(project(":api"))
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(providers.gradleProperty("testJavaVersion")
            .orElse(providers.gradleProperty("arara.jvm.target")).get().toInt()))
    })
}

tasks.register("allTests") { dependsOn(tasks.test) }

tasks.register<Jar>("runnerJar") {
    dependsOn(tasks.classes)
    archiveFileName.set("process-smoke.jar")
    manifest.attributes["Main-Class"] = "org.islandoftex.arara.smoke.ProcessSmokeKt"
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Keep the runtime-only checks tied to the shipped rule sources.
tasks.processResources {
    from(rootProject.layout.projectDirectory.dir("rules")) {
        include("arara-rule-pdflatex.*", "arara-rule-bibtex.*", "arara-rule-dvips.*",
            "arara-rule-mkdir.*", "arara-rule-rmdir.*", "arara-rule-halt.*")
        into("rules")
    }
}
