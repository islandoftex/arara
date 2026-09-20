// SPDX-License-Identifier: BSD-3-Clause
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.islandoftex.arara.build.AraraPublication
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version libs.versions.kotlin apply false // Apache 2.0
    kotlin("plugin.serialization") version libs.versions.kotlin apply false // Apache 2.0
    alias(libs.plugins.shadow) apply false // Apache 2.0
    alias(libs.plugins.versions) // Apache 2.0
    alias(libs.plugins.spotlessChangelog) // Apache 2.0
    alias(libs.plugins.dokka) apply false // Apache 2.0
    alias(libs.plugins.detekt) // Apache 2.0
    alias(libs.plugins.spotless) // Apache 2.0
}

if (!project.hasProperty("JobToken")) {
    logger.warn(
            """
            Island of TeX ----------------------------------------------
            Will be unable to publish (jobToken missing)
            Ignore this warning if you are not running the publish task
            for the GitLab package registry.
            ------------------------------------------------------------
        """.trimIndent()
    )
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
                "settings.gradle.kts",
                "buildSrc/build.gradle.kts",
                "buildSrc/settings.gradle.kts",
                "api/build.gradle.kts",
                "core/build.gradle.kts",
                "lua/build.gradle.kts",
                "mvel/build.gradle.kts",
                "cli/build.gradle.kts"
        )
        targetExclude("src/test/**/*.kts")
        // ktlint updated its rule set so that a majority of our files newly
        // triggers lints that are not as easy to fix, therefore we disabled
        // it for now
        // -----------------------------------------------------------------
        // ktlint()
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlin {
        target(
                "api/src/**/*.kt",
                "core/src/**/*.kt",
                "lua/src/**/*.kt",
                "mvel/src/**/*.kt",
                "cli/src/**/*.kt",
                "buildSrc/src/**/*.kt"
        )
        targetExclude("src/test/**/*.kts")
        // ktlint updated its rule set so that a majority of our files newly
        // triggers lints that are not as easy to fix, therefore we disabled
        // it for now
        // -----------------------------------------------------------------
        // ktlint()
        licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    yaml {
        target("rules/*.yaml")
        val header = $$"""
            !config
            # arara, the cool TeX automation tool
            # Copyright (c) $YEAR, Island of TeX
            # All rights reserved.
            #
            # This rule is part of arara.
        """.trimIndent()
        licenseHeader(header, "identifier:")
    }
    format("luaRules") {
        target("rules/*.lua")
        val header = $$"""
            -- arara, the cool TeX automation tool
            -- Copyright (c) $YEAR, Island of TeX
            -- All rights reserved.
            --
            -- This rule is part of arara.
        """.trimIndent()
        licenseHeader(header, "return \\{")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    allRules = false
    source.from(files(
            "api/src/main/kotlin",
            "core/src/main/kotlin",
            "lua/src/main/kotlin",
            "mvel/src/main/kotlin",
            "cli/src/main/kotlin",
            "buildSrc/src/main/kotlin"
    ))
    buildUponDefaultConfig = true
    config.from(files("detekt-config.yml"))
}

val validateYAML: Boolean = providers
        .gradleProperty("validateYAML")
        .map { it.toBoolean() }
        .getOrElse(false)

tasks {

    withType<DependencyUpdatesTask> {
        rejectVersionIf {
            fun isNonStable(version: String) = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                    .any { qualifier ->
                        version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                    }

            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }

        checkForGradleUpdate = false
    }

    named("spotlessYamlCheck") {
        enabled = validateYAML
    }

    named("spotlessYamlApply") {
        enabled = validateYAML
    }
}

version = spotlessChangelog.versionNext

// ----------------------------------------------------------------------
// JVM targets and Kotlin API and language versions, with corresponding
// defaults as fallback, defined in gradle.properties (see keys)
// ----------------------------------------------------------------------
val araraJVMString = runCatching { extra["arara.jvm.target"] as String }
        .getOrDefault("11")

val araraJVMTarget = runCatching { JvmTarget.fromTarget(araraJVMString) }
        .getOrDefault(JvmTarget.JVM_11)

val araraJVMInt = araraJVMString.toIntOrNull() ?: 11

val araraKotlinAPI = runCatching { extra["arara.kotlin.api"] as String }
        .getOrDefault("2.1")

val araraKotlinLanguage = runCatching { extra["arara.kotlin.language"] as String }
        .getOrDefault("2.1")
// ----------------------------------------------------------------------

allprojects {
    group = "org.islandoftex.arara"
    description = "TeX automation tool based on rules and directives"
    version = rootProject.version
}

// accessing `libs` does not work inside `subprojects` blocks (wont-fix issue),
// workaround from https://github.com/gradle/gradle/issues/18237#issuecomment-928074251
val engine = libs.junit.jupiter.engine

subprojects {

    apply(plugin = "org.jetbrains.dokka")

    val mainManifest: Manifest = DefaultManifest((project as ProjectInternal).fileResolver)
            .apply {
                attributes["Implementation-Title"] = "arara-${project.name}"
                attributes["Implementation-Version"] = version

                if (project.name == "cli") {
                    attributes["Main-Class"] = "org.islandoftex.arara.cli.CLIKt"
                }

                // for Java 9+ compatibility of log4j
                attributes["Multi-Release"] = "true"
                attributes["Automatic-Module-Name"] = rootProject.group
            }

    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<KotlinJvmProjectExtension> {

        compilerOptions {
            jvmTarget.set(araraJVMTarget)
            apiVersion.set(KotlinVersion.fromVersion(araraKotlinAPI))
            languageVersion.set(KotlinVersion.fromVersion(araraKotlinLanguage))
        }
    }

    dependencies {
        "testImplementation"(kotlin("test-junit5"))
        "testRuntimeOnly"(engine)
    }

    tasks {

        named<Test>("test") {
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
            sourceCompatibility = araraJVMString
            targetCompatibility = araraJVMString
        }

        // DGP v2 configuration (the v1 dokkaHtml task is gone as of Dokka
        // 2.2); non-public members stay undocumented by default, and the
        // platform is derived from the Kotlin JVM plugin
        configure<DokkaExtension> {
            moduleName.set("arara (${project.name})")
            dokkaSourceSets.configureEach {
                skipDeprecated.set(false)
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)
                jdkVersion.set(araraJVMInt)
                sourceLink {
                    localDirectory.set(file("./"))
                    remoteUrl.set(uri("https://gitlab.com/islandoftex/arara"))
                    remoteLineSuffix.set("#L")
                }
                enableKotlinStdLibDocumentationLink.set(true)
                enableJdkDocumentationLink.set(true)
            }
        }
    }

    apply<AraraPublication>()
}
