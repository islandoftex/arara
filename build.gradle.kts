// SPDX-License-Identifier: BSD-3-Clause
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.islandoftex.arara.build.AraraPublication
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.versions)
    alias(libs.plugins.spotlessChangelog)
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
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
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard" to "disabled",
                "ktlint_standard_import-ordering" to "enabled",
                "ij_kotlin_imports_layout" to "*"
            )
        )
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
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard" to "disabled",
                "ktlint_standard_import-ordering" to "enabled",
                "ij_kotlin_imports_layout" to "*"
            )
        )
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

val validateYamlRules: Boolean = providers
        .gradleProperty("validateYamlRules")
        .map { it.toBoolean() }
        .getOrElse(false)

val validateLuaRules: Boolean = providers
        .gradleProperty("validateLuaRules")
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
        enabled = validateYamlRules
    }

    named("spotlessYamlApply") {
        enabled = validateYamlRules
    }

    named("spotlessLuaRulesCheck") {
        enabled = validateLuaRules
    }

    named("spotlessLuaRulesApply") {
        enabled = validateLuaRules
    }
}

version = spotlessChangelog.versionNext

// ----------------------------------------------------------------------
// JVM target and Kotlin API and language versions; the keys (and their
// fallback defaults) are defined in gradle.properties
// ----------------------------------------------------------------------
val araraJVMString = providers.gradleProperty("arara.jvm.target").getOrElse("11")

val araraJVMTarget = JvmTarget.fromTarget(araraJVMString)

val araraJVMInt = araraJVMString.toIntOrNull() ?: 11

val araraKotlinAPI = providers.gradleProperty("arara.kotlin.api").getOrElse("2.1")

val araraKotlinLanguage = providers.gradleProperty("arara.kotlin.language").getOrElse("2.1")

// opt-in gate: run the build with -PwarningsAsErrors=true to prove that
// the tree compiles without a single warning
val warningsAsErrors = providers.gradleProperty("warningsAsErrors")
        .map { it.toBoolean() }
        .getOrElse(false)
// ----------------------------------------------------------------------

allprojects {
    group = "org.islandoftex.arara"
    description = "TeX automation tool based on rules and directives"
    version = rootProject.version
}

// accessing `libs` inside `subprojects` blocks does not work (wont-fix
// issue), so the test wiring relies on the Kotlin plugin helper alone

subprojects {

    apply(plugin = "org.jetbrains.dokka")

    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<KotlinJvmProjectExtension> {

        compilerOptions {
            jvmTarget.set(araraJVMTarget)
            apiVersion.set(KotlinVersion.fromVersion(araraKotlinAPI))
            languageVersion.set(KotlinVersion.fromVersion(araraKotlinLanguage))
            allWarningsAsErrors.set(warningsAsErrors)
            optIn.addAll(
                    "org.islandoftex.arara.api.localization.AraraMessages",
                    "kotlin.time.ExperimentalTime"
            )
        }
    }

    dependencies {
        "testImplementation"(kotlin("test"))
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

        withType<Jar>().configureEach {
            archiveBaseName.set("arara-${project.name}")
            manifest {
                attributes["Implementation-Title"] = "arara-${project.name}"
                attributes["Implementation-Version"] = version

                if (project.name == "cli") {
                    attributes["Main-Class"] = "org.islandoftex.arara.cli.CLIKt"
                }

                // for Java 9+ compatibility of log4j
                attributes["Multi-Release"] = "true"
                attributes["Automatic-Module-Name"] = rootProject.group
            }
        }

        withType<JavaCompile>().configureEach {
            options.release.set(araraJVMInt)
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
