// SPDX-License-Identifier: BSD-3-Clause

import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.islandoftex.arara.build.CTANTreeBuilderTask
import org.islandoftex.arara.build.CTANZipBuilderTask
import org.islandoftex.arara.build.SourceZipBuilderTask
import org.islandoftex.arara.build.TDSTreeBuilderTask
import org.islandoftex.arara.build.TDSZipBuilderTask
import org.islandoftex.arara.build.Versions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    val versions = org.islandoftex.arara.build.Versions
    kotlin("jvm") version versions.kotlin apply false                                   // Apache 2.0
    id("com.github.johnrengelman.shadow") version versions.shadow apply false           // Apache 2.0
    id("com.github.ben-manes.versions") version versions.versionsPlugin                 // Apache 2.0
    id("com.diffplug.spotless-changelog") version versions.spotlessChangelog            // Apache 2.0
    id("org.jetbrains.dokka") version versions.dokka apply false                        // Apache 2.0
    id("org.jetbrains.kotlin.plugin.serialization") version versions.kotlin apply false // Apache 2.0
    id("io.gitlab.arturbosch.detekt") version versions.detekt                           // Apache 2.0
    id("com.diffplug.gradle.spotless") version versions.spotless                        // Apache 2.0
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

spotlessChangelog {
    changelogFile("CHANGELOG.md")
    setAppendDashSnapshotUnless_dashPrelease(true)
    ifFoundBumpBreaking("breaking change")
    tagPrefix("v")
    commitMessage("Release v{{version}}")
    remote("origin")
    branch("master")
}

spotless {
    kotlinGradle {
        target("build.gradle.kts", "buildSrc/build.gradle.kts")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        target("buildSrc/src/**/*.kt")
        ktlint()
        licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    failFast = false
    input = files(
            "api/src/main/kotlin",
            "core/src/main/kotlin",
            "cli/src/main/kotlin",
            "buildSrc/src/main/kotlin"
    )
    buildUponDefaultConfig = true
    config = files("detekt-config.yml")
}

tasks.register("assembleCTANSourceZip", SourceZipBuilderTask::class.java)
tasks.register("assembleTDSTree", TDSTreeBuilderTask::class.java) {
    dependsOn(":cli:shadowJar")
    dependsOn(":docs:buildManual")
    dependsOn("assembleCTANSourceZip")
}
tasks.register("assembleTDSZip", TDSZipBuilderTask::class.java) {
    dependsOn("assembleTDSTree")
}
tasks.register("assembleCTANTree", CTANTreeBuilderTask::class.java) {
    dependsOn("assembleTDSZip")
}
tasks.register("assembleCTAN", CTANZipBuilderTask::class.java) {
    dependsOn("assembleCTANTree")
}

version = spotlessChangelog.versionNext
allprojects {
    repositories {
        jcenter()
    }

    group = "org.islandoftex.arara"
    version = rootProject.version
}
subprojects {
    if (!path.contains("docs")) {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "com.diffplug.gradle.spotless")

        dependencies {
            "implementation"(kotlin("stdlib", Versions.kotlin))
        }

        configure<SpotlessExtension> {
            java {
                removeUnusedImports()
                licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
                trimTrailingWhitespace()
                endWithNewline()
            }
            kotlin {
                ktlint()
                licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
                trimTrailingWhitespace()
                endWithNewline()
            }
            kotlinGradle {
                trimTrailingWhitespace()
                endWithNewline()
            }
        }

        val javaCompatibility = JavaVersion.VERSION_1_8
        configure<JavaPluginExtension> {
            sourceCompatibility = javaCompatibility
            targetCompatibility = javaCompatibility
        }

        tasks {
            withType<KotlinCompile> {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }

            withType<Test> {
                useJUnitPlatform()

                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                    events(TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR,
                           TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
                }
            }
        }
    }
}
