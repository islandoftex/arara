// SPDX-License-Identifier: BSD-3-Clause
plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":api"))
}

tasks.register<Jar>("runnerJar") {
    dependsOn(tasks.classes)
    archiveFileName.set("process-smoke.jar")
    manifest.attributes["Main-Class"] = "org.islandoftex.arara.smoke.ProcessSmokeKt"
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
