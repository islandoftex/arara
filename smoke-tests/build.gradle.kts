// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(providers.gradleProperty("arara.jvm.target").get().toInt())
}

dependencies {
    implementation(project(":api"))
    implementation(libs.kotlinx.serialization.json)
}

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
