import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.islandoftex.arara.build.CTANBuilderTask
import org.islandoftex.arara.build.TDSZipBuilderTask

buildscript {
    repositories {
        jcenter()
    }
}

allprojects {
    repositories {
        jcenter()
    }
    group = "com.github.cereda.arara"
}

plugins {
    kotlin("jvm") version "1.3.61" apply false                        // Apache 2.0
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false // Apache 2.0
    id("org.jetbrains.dokka") version "0.10.0" apply false            // Apache 2.0
    id("io.gitlab.arturbosch.detekt") version "1.2.0"                 // Apache 2.0
    id("com.github.ben-manes.versions") version "0.27.0"              // Apache 2.0
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

detekt {
    failFast = false
    input = files("application/src/main/kotlin",
            "buildSrc/src/main/kotlin")
    buildUponDefaultConfig = true
    config = files("detekt-config.yml")
}

tasks.register("assembleTDSZip", TDSZipBuilderTask::class.java)
tasks.register("assembleCTAN", CTANBuilderTask::class.java) {
    dependsOn(":assembleTDSZip")
}
