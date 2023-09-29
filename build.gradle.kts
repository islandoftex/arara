// SPDX-License-Identifier: BSD-3-Clause

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.islandoftex.arara.build.AraraPublication
import org.islandoftex.arara.build.CTANTreeBuilderTask
import org.islandoftex.arara.build.CTANZipBuilderTask
import org.islandoftex.arara.build.DocumentationSourceZipBuilderTask
import org.islandoftex.arara.build.SourceZipBuilderTask
import org.islandoftex.arara.build.TDSTreeBuilderTask
import org.islandoftex.arara.build.TDSZipBuilderTask
import org.islandoftex.arara.build.Versions
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

buildscript {
    repositories {
        mavenCentral()
    }
}

if (!project.hasProperty("jobToken")) {
    logger.warn(
        "Will be unable to publish (jobToken missing)\n" +
            "Ignore this warning if you are not running the publish task " +
            "for the GitLab package repository."
    )
}

plugins {
    val versions = org.islandoftex.arara.build.Versions
    kotlin("multiplatform") version versions.kotlin apply false // Apache 2.0
    kotlin("plugin.serialization") version versions.kotlin apply false // Apache 2.0
    id("com.github.johnrengelman.shadow") version versions.shadow apply false // Apache 2.0
    id("com.github.ben-manes.versions") version versions.versionsPlugin // Apache 2.0
    id("com.diffplug.spotless-changelog") version versions.spotlessChangelog // Apache 2.0
    id("org.jetbrains.dokka") version versions.dokka apply false // Apache 2.0
    id("io.gitlab.arturbosch.detekt") version versions.detekt // Apache 2.0
    id("com.diffplug.spotless") version versions.spotless // Apache 2.0
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
        target(
            "build.gradle.kts",
            "buildSrc/build.gradle.kts",
            "api/build.gradle.kts",
            "core/build.gradle.kts",
            "lua/build.gradle.kts",
            "mvel/build.gradle.kts",
            "kotlin-dsl/build.gradle.kts",
            "cli/build.gradle.kts"
        )
        targetExclude("src/test/**/*.kts")
        ktlint()
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    kotlin {
        target(
            "api/src/**/*.kt",
            "core/src/**/*.kt",
            "lua/src/**/*.kt",
            "mvel/src/**/*.kt",
            "kotlin-dsl/src/**/*.kt",
            "cli/src/**/*.kt",
            "buildSrc/src/**/*.kt"
        )
        targetExclude("src/test/**/*.kts")
        ktlint()
        licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

detekt {
    allRules = false
    source = files(
        "api/src/commonMain/kotlin",
        "api/src/jvmMain/kotlin",
        "core/src/commonMain/kotlin",
        "core/src/jvmMain/kotlin",
        "lua/src/commonMain/kotlin",
        "mvel/src/commonMain/kotlin",
        "mvel/src/jvmMain/kotlin",
        "kotlin-dsl/src/main/kotlin",
        "cli/src/commonMain/kotlin",
        "cli/src/jvmMain/kotlin",
        "buildSrc/src/main/kotlin"
    )
    buildUponDefaultConfig = true
    config = files("detekt-config.yml")
}

tasks.register("assembleCTANSourceZip", SourceZipBuilderTask::class.java)
tasks.register("assembleDocumentationSourceZip", DocumentationSourceZipBuilderTask::class.java) {
    dependsOn(":docs:buildDocs")
}
tasks.register("assembleTDSTree", TDSTreeBuilderTask::class.java) {
    dependsOn(":cli:shadowJar")
    dependsOn(":docs:buildDocs")
    dependsOn("assembleCTANSourceZip")
    dependsOn("assembleDocumentationSourceZip")
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
    group = "org.islandoftex.arara"
    description = "TeX automation tool based on rules and directives"
    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://dl.bintray.com/korlibs/korlibs/")
    }
}
subprojects {
    if (!path.contains("docs")) {
        apply(plugin = "org.jetbrains.dokka")
        val mainManifest: Manifest = DefaultManifest((project as ProjectInternal).fileResolver)
            .apply {
                attributes["Implementation-Title"] = "arara-${project.name}"
                attributes["Implementation-Version"] = version
                if (project.name == "cli") {
                    attributes["Main-Class"] = "${project.group}.Arara"
                }
                // for Java 9+ compatibility of log4j
                attributes["Multi-Release"] = "true"
                attributes["Automatic-Module-Name"] = rootProject.group
            }

        apply(plugin = "org.jetbrains.kotlin.multiplatform")
        configure<KotlinMultiplatformExtension> {
            jvm {
                withJava()
                compilations.all {
                    kotlinOptions {
                        jvmTarget = "1.8"
                    }
                }
            }

            sourceSets {
                all {
                    languageSettings.apply {
                        languageVersion = "1.9"
                        apiVersion = "1.9"
                    }
                }
                val commonMain by getting {
                    dependencies {
                        implementation(kotlin("stdlib-common"))
                    }
                }
                val commonTest by getting {
                    dependencies {
                        implementation(kotlin("test-common"))
                        implementation(kotlin("test-annotations-common"))
                    }
                }
                val jvmTest by getting {
                    dependencies {
                        implementation(kotlin("test-junit5"))
                        runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
                    }
                }
            }
        }
        tasks {
            named<Test>("jvmTest") {
                useJUnitPlatform()

                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                    events(
                        TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR,
                        TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED
                    )
                }
            }
            withType<Jar> {
                archiveBaseName.set("arara-${project.name}")
                manifest.attributes.putAll(mainManifest.attributes)
            }
            withType<JavaCompile> {
                sourceCompatibility = "1.8"
                targetCompatibility = "1.8"
            }
            named<DokkaTask>("dokkaHtml").configure {
                dokkaSourceSets.configureEach {
                    jdkVersion.set(8)
                    moduleName.set("arara (${project.name})")
                    includeNonPublic.set(false)
                    skipDeprecated.set(false)
                    reportUndocumented.set(true)
                    skipEmptyPackages.set(true)
                    platform.set(org.jetbrains.dokka.Platform.common)
                    sourceLink {
                        localDirectory.set(file("./"))
                        remoteUrl.set(uri("https://gitlab.com/islandoftex/arara").toURL())
                        remoteLineSuffix.set("#L")
                    }
                    noStdlibLink.set(false)
                    noJdkLink.set(false)
                }
            }
        }

        apply<AraraPublication>()
    }
}
