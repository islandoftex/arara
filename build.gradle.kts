import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.islandoftex.arara.build.CTANBuilderTask

buildscript {
  repositories {
    mavenCentral()
  }
}

allprojects {
  repositories {
    mavenCentral()
  }
  group = "com.github.cereda.arara"
}

plugins {
  kotlin("jvm") version "1.3.50" apply false                        // Apache 2.0
  id("com.github.johnrengelman.shadow") version "5.1.0" apply false // Apache 2.0
  id("com.github.ben-manes.versions") version "0.25.0"              // Apache 2.0
}

// exclude alpha and beta versions
// from the README (see https://github.com/ben-manes/gradle-versions-plugin)
tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA")
        .any { candidate.version.toUpperCase().contains(it) }
    val isStable = stableKeyword || "^[0-9,.v-]+$".toRegex()
        .matches(candidate.version)
    isStable.not()
  }
}

tasks.register("assembleCTAN", CTANBuilderTask::class.java)
